//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and.android;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.PriorityQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import android.os.AsyncTask;
import android.os.Handler;
import edu.mit.csail.jasongao.roadrunner.*;
import edu.mit.csail.jasongao.roadrunner.RoadRunnerService.LocationSpoofer;
import edu.mit.csail.jasongao.roadrunner.RoadRunnerService.PathSetter;
import edu.mit.csail.jasongao.roadrunner.util.LoggerI;
import edu.mit.csail.jasongao.roadrunner.util.LoggingRuntimeException;
import edu.mit.smart.sm4and.SimMobilityBroker;
import edu.mit.smart.sm4and.android.HandleOnMainThread;
import edu.mit.smart.sm4and.android.SimMobServerConnectTask.PostExecuteAction;
import edu.mit.smart.sm4and.automate.Automation;
import edu.mit.smart.sm4and.connector.Connector;
import edu.mit.smart.sm4and.connector.MinaConnector;
import edu.mit.smart.sm4and.handler.AbstractMessageHandler;
import edu.mit.smart.sm4and.handler.LocationHandler;
import edu.mit.smart.sm4and.handler.MessageHandlerFactory;
import edu.mit.smart.sm4and.handler.OpaqueReceiveHandler;
import edu.mit.smart.sm4and.handler.ReadyHandler;
import edu.mit.smart.sm4and.handler.TimeHandler;
import edu.mit.smart.sm4and.handler.WhoAreYouHandler;
import edu.mit.smart.sm4and.message.DefaultMessageTypes.IdAckMessage;
import edu.mit.smart.sm4and.message.DefaultMessageTypes.IdRequestMessage;
import edu.mit.smart.sm4and.message.DefaultMessageTypes.LocationMessage;
import edu.mit.smart.sm4and.message.DefaultMessageTypes.OpaqueReceiveMessage;
import edu.mit.smart.sm4and.message.DefaultMessageTypes.OpaqueSendMessage;
import edu.mit.smart.sm4and.message.DefaultMessageTypes.TickedSimMobMessage;
import edu.mit.smart.sm4and.json.ByteArraySerialization;
import edu.mit.smart.sm4and.json.JsonMessageParser;
import edu.mit.smart.sm4and.message.Message;
import edu.mit.smart.sm4and.message.MessageParser;

/**
 * A subclass of SimMobilityBroker which implements all of the desired functionality for 
 * Android entity interaction. Note: apps should use SimMobilityBroker to communicate with
 * Sim Mobility; this class merely contains the implementation. 
 * 
 * @author Seth N. Hetu
 */
public class AndroidSimMobilityBroker extends SimMobilityBroker {
	//Have we started yet?
	protected boolean activated = false;
	
	//We need to maintain an open connection to the Sim Mobility server, since we are in a 
	//  tight time loop.
	protected Connector conn;
	
	//For communicating back to the RoadRunner service.
	protected Handler myHandler;
	protected LoggerI logger;
	protected LocationSpoofer locspoof;
	
	//For automation
	protected int receive_counter;
	protected long automationStartMs;
	
	protected DatagramSocket serverSampleSocket;
	protected InetAddress serverSampleAddress;
	protected int serverSamplePort;
	

	private static class StartRun implements Comparable<StartRun> {
		Runnable run;
		long startTimeMs;
		StartRun(Runnable run, long startTimeMs) {
			this.run = run;
			this.startTimeMs = startTimeMs;
		}
		public int compareTo(StartRun other) {
			if (this.startTimeMs<other.startTimeMs) { return -1; }
			if (this.startTimeMs>other.startTimeMs) { return 1; }
			return 0;
		}
	}
			
	//Used for scheduling "postX()" messages.
	protected PriorityQueue<StartRun> upcomingPosts = new PriorityQueue<StartRun>();

	public void postOnHandlerDelayed(Runnable r, long delayMs) {
		upcomingPosts.add(new StartRun(r, currTimeMs+delayMs));
	}
	
	
	/**
	 * Register a custom message type and corresponding callback action.  
	 * This callback will be triggered if a message of the given type is received.
	 * @param msgType The unique identifier of the Message to handle.
	 * @param msgClass The type of this message (e.g., MyCustomMessage.class). This will 
	 *        be parsed via Gson into an appropriate message.
	 * @param handler The handler to be called when this message arrives.
	 */
	public void addCustomMessageType(String msgType, Class<? extends Message> msgClass, AbstractMessageHandler handler) {
		if (handlerFactory==null || messageParser==null) { 
			throw new LoggingRuntimeException("Cannot add a custom message type; the Sim Mobility Broker has not been initialized yet.");
		}
		
		//Add the message type.
		messageParser.addMessagetype(msgType, msgClass);

		//Add the handler.
		handler.setBroker(this);
		handlerFactory.addCustomHandler(msgType, handler);
	}

	
	public void addCustomMessageHandler(String msgType, AbstractMessageHandler handler) {
		if (handlerFactory==null || messageParser==null) { 
			throw new LoggingRuntimeException("Cannot add a custom message type; the Sim Mobility Broker has not been initialized yet.");
		}
		
		//Add the handler.
		handler.setBroker(this);
		handlerFactory.addCustomHandler(msgType, handler);
	}
	
	
	/**
	 * Create a MessageHandlerFactory with the appropriate callbacks for Android messages.
	 * @return
	 */
	protected MessageHandlerFactory makeAndroidHandlerFactory(String clientId, LocationSpoofer locSpoof) {
		//Set up our list of default handlers.
		MessageHandlerFactory res = new MessageHandlerFactory();
		res.addDefaultHandler(Message.Type.id_request, new WhoAreYouHandler(clientId), this);
		res.addDefaultHandler(Message.Type.ticked_simmob, new TimeHandler(clientId, new TimeAdvancer()), this);
		res.addDefaultHandler(Message.Type.id_ack, new ReadyHandler(), this);
		res.addDefaultHandler(Message.Type.location, new LocationHandler(locSpoof), this);
		res.addDefaultHandler(Message.Type.opaque_receive, new OpaqueReceiveHandler(new OpaqueMsgReceiver()), this);
		//res.addDefaultHandler(Message.Type.UNICAST, new UnicastHandler(), this);
		//res.addDefaultHandler(Message.Type.READY_TO_RECEIVE, new ReadyToReceiveHandler(clientId), this);
		return res;
	}
	
	protected MessageParser makeJsonMessageParser() {
		 MessageParser res = new JsonMessageParser();
		 res.addMessagetype(Message.Type.id_request, IdRequestMessage.class);
		 res.addMessagetype(Message.Type.ticked_simmob, TickedSimMobMessage.class);
		 res.addMessagetype(Message.Type.id_ack, IdAckMessage.class);
		 res.addMessagetype(Message.Type.location, LocationMessage.class);
		 res.addMessagetype(Message.Type.opaque_receive, OpaqueReceiveMessage.class);
		 //res.addMessagetype(Message.Type.UNICAST, UnicastMessage.class);
		 //res.addMessagetype(Message.Type.READY_TO_RECEIVE, ReadyToReceiveMessage.class);
		 return res;
	}
	
	private MessageParser messageParser;
	private MessageHandlerFactory handlerFactory;
	
	
	/**
	 * Initialize the Broker. Can be called multiple times, until "Connect" is called.
	 */
	public void initialize(String uniqueId, Handler myHandler, LoggerI logger, LocationSpoofer locspoof, PathSetter pathSet) {
		if (this.activated) {
			throw new LoggingRuntimeException("Can't re-initialize; SimMobilityBroker has already been activated.");
		}
		
		this.messageParser = makeJsonMessageParser();
		this.myHandler = myHandler;
		this.logger = logger;
		this.locspoof = locspoof;
		
		//Check that we have a unique ID.
		this.uniqueId = uniqueId;
		if (uniqueId==null) { throw new LoggingRuntimeException("Unique Id cannot be null."); }
		
		this.handlerFactory = makeAndroidHandlerFactory(uniqueId, locspoof);
		this.conn = new MinaConnector(this, handlerFactory);
	}
	
	public MessageParser getParser() {
		return messageParser;
	}
	
	//Handle a Message as received from MINA.
	public void handleMessage(AbstractMessageHandler handler, Message message) {
		//We want to process this on the main thread, as we may want to interact with the user.
		//Thus, we post it to the message queue.
		myHandler.post(new HandleOnMainThread(handler, message, conn, messageParser));
	}
	
	
	/**
	 * Start the SimMobility Broker (sets it to "active" and connects to the server).
	 * initialize() must be called first.
	 */
	public void activate() {
		//Silently skip if called twice.
		if (this.activated) { return; }
		
		//Connect our socket.
		//NOTE: Currently, this task will *only* end if the session is closed. 
		SimMobServerConnectTask task = new SimMobServerConnectTask(new OnConnectAction(), this.handlerFactory, logger);
		task.execute(this.conn);
		this.activated = true;
		
		//We're waiting for the server to send WHOAMI, among other things. So, we start pumping messages ourselves.
		if (Globals.SM_RERUN_FULL_TRACE) {
			this.receive_counter = 0;
			this.automationStartMs = System.currentTimeMillis();
			myHandler.post(receiveTraceR);
			
		}
	}
	
	public boolean isActive() {
		return activated;
	}
	
	
	private class DGRunTask extends AsyncTask<DatagramPacket, Void, Void> {
		protected Void doInBackground(DatagramPacket... params) {
			if (params.length!=1) { throw new LoggingRuntimeException("Only one DatagramPacket allowed."); }
			for (int i=0; i<Globals.SM_RERUN_TRAFFIC_MULTIPLIER; i++) {
				try {
					serverSampleSocket.send(params[0]);
				} catch (IOException e) {
					throw new LoggingRuntimeException(e);
				}
			}
			return null;
		}
	}

	
	
	private Runnable receiveTraceR = new Runnable() {
		public void run() {
			String line = Automation.receive_buffer.get(receive_counter);
			if (Globals.SM_NEW_BUNDLE_FORMAT) {
				throw new RuntimeException("Can't RECEIVE_TRACE_R for new bundle format (not yet supported).");
			} else {
				conn.handleBundle("12345678" + line);
			}
			
			//Send a UDP message
			if (!Globals.SM_RERUN_UDP_SERVER.isEmpty()) {
				//Init?
				if (serverSampleSocket==null) {
					try {
						serverSampleSocket = new DatagramSocket();
					} catch (SocketException e) {
						throw new LoggingRuntimeException(e);
					}
					serverSamplePort = Integer.parseInt(Globals.SM_RERUN_PORTS[RandGen.nextInt(Globals.SM_RERUN_PORTS.length)]);
					try {
						serverSampleAddress = InetAddress.getByName(Globals.SM_RERUN_UDP_SERVER);
					} catch (UnknownHostException e) {
						throw new LoggingRuntimeException(e);
					}
				}
				
				//Retrieve the latest message to send.
				int send_counter = receive_counter;
				if (send_counter<0 || send_counter>=Automation.send_buffer.size()) {
					send_counter = Automation.send_buffer.size()-1;
				}
				byte[] outgoing = Automation.send_buffer.get(send_counter).getBytes();
				
				//Send!
				DGRunTask task = new DGRunTask();
				task.execute(new DatagramPacket(outgoing, outgoing.length, serverSampleAddress, serverSamplePort));
				try {
					task.get(2000, TimeUnit.MILLISECONDS);
				} catch (TimeoutException ex) {
					throw new LoggingRuntimeException(ex);
				} catch (InterruptedException ex) {
					throw new LoggingRuntimeException(ex);
				} catch (ExecutionException ex) {
					throw new LoggingRuntimeException(ex);
				}
			}

			//Immediately post the next message.
			if (++receive_counter < Automation.receive_buffer.size()) {
				myHandler.post(this);
			} else {
				long automationEndMs = System.currentTimeMillis();
				logger.log("Automation done: " + Math.round((automationEndMs-automationStartMs)/10.0)/100.0 + " s");
			}
		}
	};
	
	
	private class OnConnectAction implements PostExecuteAction {
		@Override
		public void onPostExecute(Exception thrownException, BufferedReader reader, BufferedWriter writer) {
			//Was there an error?
			if (thrownException!=null) {
				throw new LoggingRuntimeException(thrownException);
			}
					
			//Assuming everything went ok, start our simulation loop. It looks like this:
			//   1) Send out an Async task waiting for the "tick done" message.
			//   2) When that's done, process it and send "android done" for this time step.
			//As always, messages are sent before the final "done" message.
			AndroidSimMobilityBroker.this.currTimeMs = 0;
		}
	}
	
	public class TimeAdvancer {
		public void advance(int tick, int elapsed_ms) {
	        if (elapsed_ms<=0) {
	        	throw new LoggingRuntimeException("Error: elapsed time cannot be negative.");
	        }
	        
	        //Advance
			currTimeMs += elapsed_ms;

			//Check our "to be posted" message list.
			while (upcomingPosts.peek()!=null) {
				if (upcomingPosts.peek().startTimeMs > currTimeMs) { break; }
							
				//Else, remove it and post it.
				myHandler.post(upcomingPosts.poll().run);
			}
		}
	}
	
	
	public class OpaqueMsgReceiver {
		public void receive(String fromId, String base64Data) {
			//Ignore messages sent to yourself.
			if (fromId.equals(uniqueId)) {
				logger.log("Ignoring packet sent to self.");
				return;
			}
			
			//Extract the packet.
			byte[] packet = ByteArraySerialization.Deserialize(base64Data);
			AdhocPacket p = AdhocPacketThread.ReadPacket(logger, packet, packet.length);
			
			//Send it to Road Runner's message loop as a ADHOC_PACKET_RECV.
			myHandler.obtainMessage(RoadRunnerService.ADHOC_PACKET_RECV, p).sendToTarget();
		}
	}
	

	public void forwardMessageToServer(Message obj) {
        //Append it.
        conn.addMessage(obj);
	}
	
	
	//Buffer this packet for broadcast at the end of the current time tick.
	public void sendBroadcastPacket(String myId, byte[] packet) {
		//A "broadcast" packet looks something like this:
		//ANDROID_BROADCAST:ID:[DATA]
		//...where "DATA" is just a byte-array serialized in some form and opaque to Sim Mobility
		//   (it just passes it along), and "ID" is the ID of the emulator in question, so we
		//   can ignore packets to ourselves.
		//We use brackets around DATA to help ensure that we're not deserializing random junk.
		if (myId==null || packet==null) { throw new LoggingRuntimeException("Can't broadcast without data or an id."); }
		
		//Prepare the packet.
		OpaqueSendMessage obj = new OpaqueSendMessage();
		obj.data = ByteArraySerialization.Serialize(packet);
		obj.broadcast = true;
		obj.from_id = uniqueId;
		
		//Save it for later.
		conn.addMessage(obj);
	}
	
    //Helper: Same, but a bit easier to use given an object.
    public void ReflectToServer(String msg) {
    	SimMobilityBroker.ReflectToServer(conn, uniqueId, msg);
    }

}

//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and.connector;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import edu.mit.csail.jasongao.roadrunner.Globals;
import edu.mit.csail.jasongao.roadrunner.util.LoggingRuntimeException;
import edu.mit.smart.sm4and.message.Message;
import edu.mit.smart.sm4and.message.MessageParser;
import edu.mit.smart.sm4and.message.MessageParser.MessageBundle;


/**
 * Contains our custom protocol for coordinating between Sim Mobility and the client.
 */
public class MinaProtocols implements ProtocolCodecFactory {
    private ProtocolEncoder encoder;
    private ProtocolDecoder decoder;
    
    //TODO: We need the "MessageTypes" here. Perhaps we can migrate them from JsonMessageParser? That class doesn't do much any more.
    private MessageParser rootParser;

    public MinaProtocols(MessageParser parser) {
        encoder = new MinaProtocolEncoder();
        decoder = new MinaProtocolDecoder();
        rootParser = parser;
    }

    public ProtocolEncoder getEncoder(IoSession ioSession) throws Exception {
        return encoder;
    }

    public ProtocolDecoder getDecoder(IoSession ioSession) throws Exception {
        return decoder;
    }
    
    private class MinaProtocolEncoder implements ProtocolEncoder {

        public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
        	MessageBundle bundle = (MessageBundle)message;

        	//Create the data section first.
        	byte[] data = null;
        	if (Globals.SM_NEW_BUNDLE_FORMAT) {
        		ByteArrayOutputStream vary = new ByteArrayOutputStream();
        		ByteArrayOutputStream msgs = new ByteArrayOutputStream();
        		
            	Gson gson = new Gson();
            	for (Message msg : bundle.messages) {
            		String msgStr = gson.toJsonTree(msg).toString();
            		vary.write((msgStr.length()>>16)&0xFF);
            		vary.write((msgStr.length()>>8)&0xFF);
            		vary.write(msgStr.length()&0xFF);
            		msgs.write(msgStr.getBytes());
            	}
            	vary.write(bundle.sendId.getBytes());
            	vary.write(bundle.destId.getBytes());
            	vary.write(msgs.toByteArray());
            	data = vary.toByteArray();
        	} else {
            	JsonObject packet = new JsonObject();
            	serializeHeader(packet, bundle.sendId, bundle.destId);
            	serializeData(packet, bundle.messages);
            	data = packet.toString().getBytes();
        	}
        	
        	//Now, create an appropriately-formatted header.
        	ByteArrayOutputStream head = new ByteArrayOutputStream();
        	if (Globals.SM_NEW_BUNDLE_FORMAT) {
        		head.write(1);
        		head.write(bundle.messages.size());
        		head.write(bundle.sendId.length());
        		head.write(bundle.destId.length());
        		head.write((data.length>>24)&0xFF);
        		head.write((data.length>>16)&0xFF);
        		head.write((data.length>>8)&0xFF);
        		head.write(data.length&0xFF);
        	} else {
        		head.write(String.format("%8h", data.length+1).getBytes());
        	}
        	
        	//Quick check.
        	if (head.size() != 8) { 
        		throw new LoggingRuntimeException("Error; header is " + head.size() + "bytes, not 8."); 
        	}
        	
        	//Now, convert it to an IoBuffer.
            IoBuffer buffer = IoBuffer.allocate(head.size() + data.length, false);
            buffer.put(head.toByteArray());
            buffer.put(data);
            buffer.flip();
            out.write(buffer);
        }
        
        private void serializeHeader(JsonObject res, String sendId, String destId) {
            JsonObject header = new JsonObject();
            header.addProperty("send_client", sendId);
            header.addProperty("dest_client", destId);
            res.add("header", header);
        }
        
        private void serializeData(JsonObject res, ArrayList<Message> messages) {
        	Gson gson = new Gson();
        	JsonArray data = new JsonArray();
        	for (Message msg : messages) {
        		data.add(gson.toJsonTree(msg));
        	}
            res.add("messages", data);
        }

        public void dispose(IoSession session) throws Exception {
            // nothing to dispose
        }
    }
    
    private class MinaProtocolDecoder extends CumulativeProtocolDecoder {
    	//These are always used.
    	byte[] currHeader = null;
    	byte[] currData = null;
    	int dataSize = 0;
    	
    	//These are only part of v1.
		int numMsgs = 0;
		int sendIdLen = 0;
		int destIdLen = 0;
    	
        protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
        	//Are we reading the header?
        	if (currHeader==null) {
        		//Read the header.
        		if (in.remaining() < 8) { return false; }
        		currHeader = new byte[8];
        		in.get(currHeader);
        		
        		//Determine the amount of remaining data.
        		if (Globals.SM_NEW_BUNDLE_FORMAT) {
        			//Check version
        			if (currHeader[0] != 1) { throw new LoggingRuntimeException("Error: header version 1 expected."); }
        			
        			//Retrieve lengths.
        			numMsgs = currHeader[1];
        			sendIdLen = currHeader[2];
        			destIdLen = currHeader[3];
        			dataSize = ((((int)currHeader[4])&0xFF)<<24) | ((((int)currHeader[5])&0xFF)<<16) | ((((int)currHeader[6])&0xFF)<<8) | (((int)currHeader[7])&0xFF);
        		} else {
        			//8-byte "text" hex string.
        			dataSize = Integer.parseInt(new String(currHeader), 0x10);
        		}
        		
        		//Expect to read data next.
        		currData = null;
        	}
        	
        	//Now we are definitely reading the data section.
        	if (in.remaining() < dataSize) { return false; }
        	currData = new byte[dataSize];
        	in.get(currData);
        	
        	//Turn this into a MessageBundle
        	MessageBundle res = null;
        	if (Globals.SM_NEW_BUNDLE_FORMAT) {
        		res = parse_v1(numMsgs, sendIdLen, destIdLen, new ByteArrayInputStream(currData));
        	} else {
        		res = parse_v0(new String(currData));
        	}
        	
        	//Make sure this worked.
        	if (res==null) {
        		throw new LoggingRuntimeException("Couldn't deserialize MessageBundle");
        	}
        	
        	//Done, signal success.
            out.write(res);
            return true;
        }
        
        
        private byte[] readBytes(ByteArrayInputStream data, int numBytes) {
        	byte[] res = new byte[numBytes];
        	try {
        		if (data.read(res) < res.length) { throw new LoggingRuntimeException("Can't parse message; not enough bytes (" + numBytes + ")"); }
        	} catch (IOException ex) {
        		throw new LoggingRuntimeException("IOException while reading bundled bytes.");
        	}
        	return res;
        }
        
        ///Useful for some debugging operations
        @SuppressWarnings("unused")
		private String print_ints(char[] data) {
        	StringBuilder sb = new StringBuilder();
        	sb.append("[");
        	for (char c : data) {
        		sb.append(((int)c));
        		sb.append(" , ");
        	}
        	sb.append("]");
        	return sb.toString();
        }
        
        private MessageBundle parse_v1(int numMsgs, int sendIdLen, int destIdLen, ByteArrayInputStream data) {
        	//For debugging v1 headers:
        	/*System.out.println("Header: " + header);
        	System.out.println("   int: " + print_ints(header.toCharArray()));
        	System.out.println("Data: " + data);
        	System.out.println(" int: " + print_ints(data.toCharArray()));*/
        	
        	//Extract message lengths. Start tracking the current offset.
        	ArrayList<Integer> msgLengths = new ArrayList<Integer>();
        	for (int n=0; n<numMsgs; n++) {
        		msgLengths.add(((data.read()&0xFF)<<16) | ((data.read()&0xFF)<<8) | (data.read()&0xFF));
        	}
        	
        	//Extract send/dest IDs.
        	MessageBundle res = new MessageBundle();
        	res.sendId = new String(readBytes(data, sendIdLen));
        	res.destId = new String(readBytes(data, destIdLen));
        	
        	//Iterate through each message and add an appropriate Message type to the result list.
        	Gson gson = new Gson();
        	for (int len : msgLengths) {
        		//Peek the next character.
        		data.mark(-1);
        		int firstChar = data.read()&0xFF;
        		data.reset();
        		
        		if (firstChar == 0xBB) {
        			throw new LoggingRuntimeException("Binary message format not yet supported.");
        		} else if (firstChar == '{') {
                	//First, parse it as a generic "message" object.
        			String msg = new String(readBytes(data, len));
                	Message rawObject = gson.fromJson(msg, Message.class);

                	//Depending on the type, re-parse it as a sub-class.
                	Class<? extends Message> msgClass = rootParser.GetClassFromType(rawObject.getMessageType());
                	Message specificObject = null;
                	try {
                		specificObject = gson.fromJson(msg, msgClass); //This line is failing for the new message type.
                	} catch (JsonSyntaxException ex) {
                		ex.printStackTrace();
                		throw new LoggingRuntimeException(ex);
                	}
                	res.messages.add(specificObject);
        		} else {
        			throw new LoggingRuntimeException("Could not determine message format.");
        		}
        	}
        	
        	return res;
        }
        
        private MessageBundle parse_v0(String data) {
        	//For some reason, a few Apache Mina implementations include lots of garbage between
        	// the trailing right-brace and the newline. This is obnoxious for a number of reasons,
        	// but primarily in that it breaks Gson parsing. We solve this by manually scanning
        	// for the closing brace and cutting everything after that. This *might* disable
        	// muultiple messages on the same stream, but for now that's not a problem.
        	//NOTE: This isn't necessary for v1, where each message is an exact, known size.
        	data = MessageParser.FilterJson(data);
        	
            //Parse the message into a generic Json object.
            JsonParser parser = new JsonParser();
            JsonObject root = (JsonObject)parser.parse(data);
                    
            //Ensure we have a few mandatory properties.
            JsonObject head = root.getAsJsonObject("header");
            JsonArray messages = root.getAsJsonArray("messages");
            if (head==null) { throw new LoggingRuntimeException("Packet arrived with no header."); }
            if (messages==null) { throw new LoggingRuntimeException("Packet arrived with no messages."); }
                    
        	//Set sender/dest properties.
        	if (!(head.has("send_client") && head.has("dest_client"))) { throw new LoggingRuntimeException("Header missing fields."); }
        	MessageBundle res = new MessageBundle();
        	res.sendId = head.get("send_client").getAsString();
        	res.destId = head.get("dest_client").getAsString();

        	//Iterate through each DATA element and add an appropriate Message type to the result list.
        	Gson gson = new Gson();
            for (JsonElement msg : messages) {
            	//First, parse it as a generic "message" object.
            	Message rawObject = gson.fromJson(msg, Message.class);

            	//Depending on the type, re-parse it as a sub-class.
            	Class<? extends Message> msgClass = rootParser.GetClassFromType(rawObject.getMessageType());
            	Message specificObject = null;
            	try {
            		specificObject = gson.fromJson(msg, msgClass); //This line is failing for the new message type.
            	} catch (JsonSyntaxException ex) {
            		ex.printStackTrace();
            		throw new LoggingRuntimeException(ex);
            	}
            	res.messages.add(specificObject);
            }
            
            //Done
            return res;
        }
    }
}

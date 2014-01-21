//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and.message;

import edu.mit.smart.sm4and.message.Message;

/** Simple remote-Log message. */
public class RemoteLogMessage extends Message {	
	public RemoteLogMessage(String uniqueId, String log_message) { 
		super(Type.REMOTE_LOG, uniqueId);
		this.log_message = log_message;
	}
	
    public String log_message;
    
	//This constructor is only used by GSON
	@SuppressWarnings("unused")
	private RemoteLogMessage() { this("0", ""); }
}   

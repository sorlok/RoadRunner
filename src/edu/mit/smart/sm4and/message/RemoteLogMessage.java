//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and.message;

import edu.mit.smart.sm4and.message.Message;

/** Simple remote-Log message. */
public class RemoteLogMessage extends Message {	
	public RemoteLogMessage() { 
		super(Type.remote_log);
	}
	
    public String log_msg;
}   

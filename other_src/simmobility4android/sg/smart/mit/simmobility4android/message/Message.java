/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sg.smart.mit.simmobility4android.message;

/**
 *
 * @author gandola
 */
public class Message {

    public enum MessageType {
        WhoAreYou,
        TimeData,
        Ready,
        LocationData
    }

    public MessageType getMessageType() {
        return MessageType;
    }

    public void setMessageType(MessageType MessageType) {
        this.MessageType = MessageType;
    }
    
    private MessageType MessageType;
}

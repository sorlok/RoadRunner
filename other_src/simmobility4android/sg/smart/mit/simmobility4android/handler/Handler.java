package sg.smart.mit.simmobility4android.handler;

import sg.smart.mit.simmobility4android.connector.Connector;
import sg.smart.mit.simmobility4android.message.Message;

/**
 * Interface for all handler implementations.
 * 
 * Note that the message can only be set in the constructor (which is protected), 
 * and then only retrieved with the "getMessage()" function. Thus, we avoid the need
 * for a generic <T extends Message> which really only serves to simplify "getMessage()".
 * Now, you can just have the subclass cast the result of "getMessage()".
 *
 * @author gandola
 */
public abstract class Handler {
    private final Message message;
    private final Connector connector;

    protected Handler(Message message, Connector connector) {
        this.message = message;
        this.connector = connector;
    }

    public abstract void handle();

    protected Connector getConnector() {
        return connector;
    }

    public Message getMessage() {
        return message;
    }
}

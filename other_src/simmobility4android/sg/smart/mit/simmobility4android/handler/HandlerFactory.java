package sg.smart.mit.simmobility4android.handler;

import sg.smart.mit.simmobility4android.connector.Connector;

public interface HandlerFactory {

    Handler create(Connector connector, Object message, int ID);
}

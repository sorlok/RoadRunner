//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package sg.smart.mit.simmobility4android.handler;

import sg.smart.mit.simmobility4android.connector.Connector;

/**
 * @author Pedro Gandola
 * @author Vahid
 */
public interface HandlerFactory {
    Handler create(Connector connector, String message, int ID);
}

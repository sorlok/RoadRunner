//Copyright (c) 2013 Singapore-MIT Alliance for Research and Technology
//Licensed under the terms of the MIT License, as described in the file:
//   license.txt   (http://opensource.org/licenses/MIT)

package edu.mit.smart.sm4and.message;

/**
 * @author Vahid
 */
public class LocationMessage extends Message{
    
    private int x;
    private int y;
    
    public int getX(){
        return x;
    }
    
    public int getY(){
        return y;
    }
}

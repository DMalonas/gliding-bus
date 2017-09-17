/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MyClasses;

import java.io.Serializable;

/**
 *
 * @author dmalonas
 */

// This class facilitates the conditional availabilituy of flight types per club
public class Club implements Serializable
{
    private String name;
    private boolean aerotowAvailable;
    private boolean winchAvailable;
    
    public Club(String name, boolean aerotowAvailable, boolean winchAvailable)
    {
        this.name = name;
        this.aerotowAvailable = aerotowAvailable;
        this.winchAvailable = winchAvailable;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    public String getName()
    {
        return name;
    }
    
    public void setAerotowAvailable(boolean aerotowAvailable)
    {
        this.aerotowAvailable = aerotowAvailable;
    }
    
    public boolean getAerotowAvailable()
    {
        return aerotowAvailable;
    }
    
    public void setWinchAvailable(boolean name)
    {
        this.winchAvailable = winchAvailable;
    }
    
    public boolean getWinchAvailable()
    {
        return winchAvailable;
    }
}

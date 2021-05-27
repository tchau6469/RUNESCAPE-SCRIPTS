package scripts;

import org.tribot.api2007.types.RSTile;

public class Location {
    public RSTile tile;


    //initially not in use when the location is made
    public boolean inUse = false;

    public Location(int xCoord, int yCoord, int zCoord) {
        this.tile = new RSTile(xCoord, yCoord, zCoord);

    }
}

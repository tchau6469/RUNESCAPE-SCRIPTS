package scripts;

import org.tribot.api2007.types.RSTile;

public class Trap {
    public enum STATE {Wait, Success, Failed, Ground, NA};
    public STATE currentState = STATE.Wait;
    public RSTile locationOnGround;




    public Trap (RSTile location) {

        this.locationOnGround = location;
    }


}

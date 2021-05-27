package scripts;

import org.tribot.api.Clicking;
import org.tribot.api.General;
import org.tribot.api.interfaces.Positionable;
//import org.tribot.api2007.*;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.Player;
import org.tribot.api2007.types.RSItem;
import org.tribot.api2007.types.RSNPC;
import org.tribot.api2007.types.RSTile;
import org.tribot.script.Script;
import org.tribot.api.Timing;
import org.tribot.api.DynamicClicking;
import org.tribot.script.interfaces.MessageListening07;

public class CeruleanTwitches extends Script{

    boolean RUNNING = true;

    enum TASK {Placing, Checking, Dismantling, Laying, Dropping, Waiting}
    final static int xMinCoordinate = 2338;
    final static int xMaxCoordinate = 2344;
    final static int yMaxCoordinate = 3600;
    final static int yMinCoordinate = 3596;
    final static int zCoordinate = 0;

    final int birdSnareItemID = 10006;
    final int placedBirdSnareID = 9345;
    final int birdSnareFailedID = 9344;
    final int birdSnareSuccessID = 9379;

    final int bonesID = 526;
    final int birdMeatID = 9978;

    int numBirdSnaresPlaced = 0;

    TASK currentTask = TASK.Placing;

    static RSTile location = new RSTile(General.random(xMinCoordinate, xMaxCoordinate), General.random(yMinCoordinate, yMaxCoordinate), zCoordinate);



    public void run() {
        while(RUNNING){
            switch (currentTask) {
                case Placing:
                    //player is not on top of the birdsnare tile
                    if (Player.getPosition() != location.getPosition()) {
                        walkToTile(location);

                        //keeping sleeping until player stops moving
                        while (Player.isMoving()) {
                            General.sleep(100);
                        }

                        //wait for a second or two after getting the location
                        General.sleep(General.randomSD(1000, 2000, 1200, 1200));


                    }

                    RSItem[] birdSnaresArray = Inventory.find(birdSnareItemID);

                    //bird snares exist, place that shit down
                    if (birdSnaresArray.length > 0) {
                        Clicking.click(birdSnaresArray[0]);
                    }

                    currentTask = TASK.Waiting;

                    //waiting for trap to succeed, fail, or timeout
                case Waiting:

                    //checking is available when bird snare succeeds
                case Checking:

                    //dismantling is available when  bird snare fails
                case Dismantling:

                    //laying is available when bird snare times out (item is on ground)
                case Laying:

                    //when inv is full of chicken shit (need 3 inv spots for each bird (bones, meat, snare)
                case Dropping:


            }//end of switch statements
        }//end of while(RUNNING)

    }

    public void walkToTile (RSTile tile) {
        if (tile.isOnScreen()&& tile.isClickable()) {
            Clicking.click(tile);
        }
    }
}


package scripts;

import org.tribot.api.Clicking;
import org.tribot.api.DynamicClicking;
import org.tribot.api.General;
import org.tribot.api.input.Mouse;
import org.tribot.api.util.abc.ABCProperties;
import org.tribot.api.util.abc.ABCUtil;
import org.tribot.api2007.GroundItems;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.Objects;
import org.tribot.api2007.Player;
import org.tribot.api2007.types.RSGroundItem;
import org.tribot.api2007.types.RSItem;
import org.tribot.api2007.types.RSObject;
import org.tribot.api2007.types.RSTile;
import org.tribot.script.Script;
import org.tribot.api.Timing;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;

public class Chinchompas extends Script{

    public static void main(String[] args) {}

    boolean RUNNING = true;

    enum TASK {PlaceTrap(2), ResetTrap(5), LayTrap (4), Waiting(1);
        public int weight = 0;
        public Trap trapToHandle = null;
        TASK(int weight) {
            this.weight = weight;
        }

        public TASK setTrapToHandle(Trap trapToHandle) {
            this.trapToHandle = trapToHandle;
            return this;
        }
    };

    final int boxTrapItemID = 10008;      // In inventory
    final int boxTrapGroundID = 10008;     // On ground, inactive (item on ground)
    final int boxTrapWaitID = 9380;       // On ground placed  (yellow)
    final int boxTrapFailedID = 9385;     // On ground failed   (red)
    final int boxTrapSuccessID = 9382;    // On ground success (green)

    int maxNumberOfTraps = 3;

    ABCUtil abc_util = null;

    long timeSinceLastAction = 0;

    ArrayList<Trap> trapsPlacedArray = new ArrayList<>();
    PriorityQueue<TASK> tasksToDo = new PriorityQueue<>(11, new Comparator<TASK>() {
        @Override
        public int compare(TASK o1, TASK o2) {
            return o2.weight - o1.weight;
        }
    });

    //initial = 2342, 3590, 0
    static ArrayList<Location> locations = new ArrayList<Location>() {
        {
            add(new Location(2341, 3590, 0));
            add(new Location(2343, 3590, 0));
            add(new Location(2342, 3591, 0));
            //add(new Location(2342, 3591, 0));
            //add(new Location(1, 1, 0));
            //add(new Location(1, 1, 0));
        }
    };

    public void run() {

        // Start anti-ban
        //startABCUtil();

        // Start filling initial tasks
        whatToDo();

        System.out.println("initialized :)");

        while(RUNNING){

            if (!tasksToDo.isEmpty()) {
                tasksToDo.add(TASK.Waiting);
            }

            TASK taskToHandle = tasksToDo.poll();
            System.out.println("tasked popped");



            switch (taskToHandle) {

                case PlaceTrap:
                    // Place traps down

                    Location openLocation = null;

                    // get next open location
                    for(Location loc : locations){
                        if(!loc.inUse) {
                            openLocation = loc;
                            loc.inUse = true;
                            break;
                        }
                    }

                    // Check if open location
                    if(openLocation == null){
                        System.out.println("No open location left?? ERROR");
                        return;
                    }

                    // player is not on top of the box trap tile
                    if (Player.getPosition() != openLocation.tile.getPosition()) {
                        walkToTile(openLocation.tile);

                        // keeping sleeping until player stops moving
                        while (Player.getPosition().getX() != openLocation.tile.getX() && Player.getPosition().getY() != openLocation.tile.getY()) {
                            System.out.println("PLAYER IS MOVING");
                            General.sleep(60, 120);
                        }

                        System.out.println("PLAYER REACHED TRAP TILE");
                        //wait for a second or two after getting to the destination
                        General.sleep(General.randomSD(1000, 2000, 1200, 1200));
                    }

                    System.out.println("attemping to place tile");
                    RSItem[] boxTrapInInventory = Inventory.find(boxTrapItemID);

                    // box traps exist in inventory, place that down
                    if (boxTrapInInventory.length > 0) {
                        Clicking.click(boxTrapInInventory[0]);
                        trapsPlacedArray.add(new Trap(openLocation.tile.getPosition()));

                        General.sleep(600, 1000);

                        while (Player.getAnimation() > -1 ) {
                            General.sleep(60, 120);
                        }
                    }

                    timeSinceLastAction = Timing.currentTimeMillis();

                    break;

                //waiting for any trap to succeed, fail, or timeout
                case Waiting:

//                    final int est_waiting = getWaitingTime();
//                    //else est_waiting = 3000; // An arbitrary value, only used at the very beginning of the script
//                    final ABCProperties props = this.abc_util.getProperties();
//                    props.setWaitingTime(est_waiting);
//                    props.setUnderAttack(false);
//                    props.setWaitingFixed(false);
//                    this.abc_util.generateTrackers();


                    Timing.waitCondition(()-> {General.sleep(50, 60); return isThereSomethingToDo();}, General.randomSD(60000, 65000, 62000, 62000));

                    // At this point there is something to do so we should add it to queue
                    whatToDo();

                    break;

                //Resetting is available when box trap succeeds or fails
                case ResetTrap:

                    hoverTrapAndClick("Reset",  taskToHandle.trapToHandle);
                    taskToHandle.trapToHandle.currentState = Trap.STATE.Wait;

                    timeSinceLastAction = Timing.currentTimeMillis();

                    break;

                //laying is available when box trap times out (item is on ground)
                case LayTrap:

                    hoverTrapAndClick("Lay",  taskToHandle.trapToHandle);
                    taskToHandle.trapToHandle.currentState = Trap.STATE.Wait;

                    timeSinceLastAction = Timing.currentTimeMillis();

                    break;



            }//end of switch statements

            while(doingAnimation() || Player.isMoving()) {
                General.sleep(60,100);
            }

            //add a delay after doing an action to look human
            General.sleep(General.randomSD(700,1100, 850, 850));

//            final int waiting_time = getWaitingTime();
//            final boolean menu_open = this.abc_util.shouldOpenMenu() && this.abc_util.shouldHover();
//            final boolean hovering = this.abc_util.shouldHover(); // If the condition is met, we specify the relevant flag, otherwise we set the variable to 0
//            // When we pass 0 into generateReactionTime as a bit flag option, it will not change anything
//            final long hover_option = hovering ? ABCUtil.OPTION_HOVERING : 0;
//            final long menu_open_option = menu_open ? ABCUtil.OPTION_MENU_OPEN : 0;
//            // Generate the reaction time
//            final int reaction_time = this.abc_util.generateReactionTime(this.abc_util.generateBitFlags(waiting_time, hover_option, menu_open_option));
//            // Sleep for the reaction time
//            try {
//                this.abc_util.sleep(reaction_time);
//            } catch (final InterruptedException e) {
//                System.out.println("Could not sleep reaction time...");
//            }

        }//end of while(RUNNING)

        // end abcUtil
        //endABCUtil();

    }

    public void hoverTrapAndClick(String option, Trap trap){
        // Add rng mouse clicks?
        Clicking.click(option, trap.locationOnGround);
    }

    public void walkToTile (RSTile tile) {
        if (tile.isOnScreen()) {
            DynamicClicking.clickRSTile(tile, 1);
        }
    }

    // Function looks at the objects on the RSTile
    public Trap.STATE determineStateOfTrap(Trap trap){

        //INDIVIDUAL CHECK FOR GROUND ITEM BIRD SNARE, SINCE GROUND ITEMS ARE NOT RSOBJECTS
        RSGroundItem[] groundItems = GroundItems.getAt(trap.locationOnGround);

        for (RSGroundItem gItem: groundItems) {
            if (gItem.getID() == boxTrapGroundID) {
                return Trap.STATE.Ground;
            }
        }

        // Get all the objects located on the trap
        RSObject[] objectsOnTrap = Objects.getAt(trap.locationOnGround);

        for(RSObject object : objectsOnTrap){
            if(object.getID() == boxTrapWaitID)
                return Trap.STATE.Wait;
            else if(object.getID() == boxTrapFailedID) {
                System.out.println("TRAP FAILED");
                return Trap.STATE.Failed;}
            else if(object.getID() == boxTrapSuccessID)
                return Trap.STATE.Success;
            else if(object.getID() == boxTrapGroundID)
                return Trap.STATE.Ground;
        }
        return Trap.STATE.NA;
    }

    public boolean isThereSomethingToDo () {
        for (Iterator<Trap> trapsItr = trapsPlacedArray.iterator(); trapsItr.hasNext();) {
            Trap trap = trapsItr.next();

            // Find the state of the trap
            trap.currentState = determineStateOfTrap(trap);

            // If any trap is not waiting then we have something to do
            if (trap.currentState != Trap.STATE.Wait) {
                return true;
            }
            else if(trap.currentState == Trap.STATE.NA){
                // At this point, the trap doesnt exist...
                // We should remove from our list...

//                int xCoord = trap.locationOnGround.getX();
//                int yCoord = trap.locationOnGround.getY();

//                //since the trap doesnt exist on that location, the location is not in use, so it is available for trap placement.
//                for (Location location : locations) {
//                    if (xCoord == location.tile.getX() && yCoord == location.tile.getY()) {
//                        location.inUse = false;
//                        break;
//                    }
//                }
                trapsItr.remove();

                //a trap disappeared, so location is empty. return true, cuz we got an empty location
                return true;

            }
        }

        // Do other things here, such as hovering and menu opening if the mouse is still in the game screen boundary
//        if (Mouse.isInBounds()) {
//            if (this.abc_util.shouldCheckTabs())
//                this.abc_util.checkTabs();
//
//            if (this.abc_util.shouldCheckXP())
//                this.abc_util.checkXP();
//
//            if (this.abc_util.shouldMoveMouse())
//                this.abc_util.moveMouse();
//
//            if (this.abc_util.shouldPickupMouse())
//                this.abc_util.pickupMouse();
//
//            if (this.abc_util.shouldRightClick())
//                this.abc_util.rightClick();
//
//            if (this.abc_util.shouldRotateCamera())
//                this.abc_util.rotateCamera();
//
//            if (this.abc_util.shouldLeaveGame())
//                this.abc_util.leaveGame();
//        }
        return false;
    } //end of isThereSomethingToDo

    public void whatToDo() {
        //number of placed traps is less than max number of traps available, so add some shit
        for  (int i = trapsPlacedArray.size(); i < maxNumberOfTraps; i++) {
            tasksToDo.add(TASK.PlaceTrap);
        }


        for (Trap trap : trapsPlacedArray) {
            switch (trap.currentState) {
                case Success:
                    tasksToDo.add(TASK.ResetTrap.setTrapToHandle(trap));
                    break;
                case Failed:
                    tasksToDo.add(TASK.ResetTrap.setTrapToHandle(trap));
                    break;
                case Ground:
                    tasksToDo.add(TASK.LayTrap.setTrapToHandle(trap));
                    //case for expired trap
                    break;
                default:
                    break;

            }//end of switch statement
        }//end of for loop

        //priority queue is empty, so wait (no tasks, gotta wait for traps)
        if (tasksToDo.isEmpty()) {
            tasksToDo.add(TASK.Waiting);
        }


    }//end of whatToDo()

    //method to check if player is in an animation
    public boolean doingAnimation() {
        return Player.getAnimation() > -1;
    }


//    void startABCUtil(){
//
//        if(abc_util != null){
//            abc_util.close();
//            abc_util = null;
//        }
//
//        abc_util = new ABCUtil();
//    }
//
//    void endABCUtil(){
//        if(abc_util != null){
//            abc_util.close();
//            abc_util = null;
//        }
//    }
//
//    int getWaitingTime(){
//        return (int)(Timing.currentTimeMillis() - timeSinceLastAction);
//    }

}

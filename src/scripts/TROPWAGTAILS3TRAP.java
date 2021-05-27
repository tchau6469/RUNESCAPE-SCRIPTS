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

public class TROPWAGTAILS3TRAP extends Script{



    boolean RUNNING = true;

    //data members for determining random trap locations
    final static int xMinCoordinate = 2338;
    final static int xMaxCoordinate = 2344;
    final static int yMaxCoordinate = 3600;
    final static int yMinCoordinate = 3596;
    final static int zCoordinate = 0;

    final static int birdSnareItemID = 10006;
    final static int placedBirdSnareID = 9345;
    final static int birdSnareFailedID = 9344;
    final static int birdSnareSuccessID = 9348;
    final static int birdSnareIgnore = 9347;
    final static int birdSnareIgnore2 = 9346;

    final static int bonesID = 526;
    final static int birdMeatID = 9978;

    enum TASK {PlaceTrap(2), CheckAndPlaceTrap(5), LayTrap (4), Waiting(1), DismantleAndPlaceTrap(3), Dropping(6);
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

//    final int boxTrapItemID = 10008;      // In inventory
//    final int boxTrapGroundID = 10008;     // On ground, inactive (item on ground)
//    final int boxTrapWaitID = 9380;       // On ground placed  (yellow)
//    final int boxTrapFailedID = 9385;     // On ground failed   (red)
//    final int boxTrapSuccessID = 9382;    // On ground success (green)

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
            add(new Location(2523, 2935, 0));
            add(new Location(2524, 2935, 0));
            add(new Location(2525, 2935, 0));
            //add(new Location(1, 1, 0));
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
                    placeDownTrap();


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

                    //checks if the isThereSomethingToDo() method returns true solely because of inventory being full
                    //if the the inventory is full, then break out of the waiting phase and not execute whatToDo(), since it will check on next iteration of poll()
                    if (isInventoryFull()) {
                        //adding drop inventory to task queue
                        tasksToDo.add(TASK.Dropping);
                        break;
                    }


                    // At this point there is something to do so we should add it to queue
                    whatToDo();

                    break;

                //Resetting is available when box trap succeeds or fails
                case CheckAndPlaceTrap:

                    if (taskToHandle.trapToHandle == null) {System.out.println("THERE IS NO STRAP TO HANDLE");}

                    System.out.println("TRAPS XCOORD IS: " + taskToHandle.trapToHandle.locationOnGround.getX());
                    System.out.println("TRAPS YCOORD IS: " + taskToHandle.trapToHandle.locationOnGround.getY());
                    System.out.println("CHECKING TRAP, GONNA CLICK ON CHECK");
                    //NULL HERE

//                    System.out.println("TRAPS XCOORD IS: " + taskToHandle.trapToHandle.locationOnGround.getX());
//                    System.out.println("TRAPS YCOORD IS: " + taskToHandle.trapToHandle.locationOnGround.getY());
                    Clicking.click("Check", taskToHandle.trapToHandle.locationOnGround);
                    System.out.println("SUCCESSFULLY CLICKED ON CHECK");
                    General.sleep(General.randomSD(700,1000,800,800));
                    while(doingAnimation() || Player.isMoving()) {
                        General.sleep(60,100);
                    }

                    General.sleep(General.randomSD(700,1000,800,800));
                    RSTile aTile = taskToHandle.trapToHandle.locationOnGround;

                    System.out.println("TRAPS NEW XCOORD IS: " + aTile.getX());
                    System.out.println("TRAPS NEW YCOORD IS: " + aTile.getY());

                    placeDownTrap(new Location(aTile.getX(), aTile.getY(), 0));

                    taskToHandle.trapToHandle.currentState = Trap.STATE.Wait;

                    timeSinceLastAction = Timing.currentTimeMillis();

                    break;

                //laying is available when box trap times out (item is on ground)
                case LayTrap:

                    hoverTrapAndClick("Lay",  taskToHandle.trapToHandle);
                    taskToHandle.trapToHandle.currentState = Trap.STATE.Wait;

                    timeSinceLastAction = Timing.currentTimeMillis();

                    break;

                case DismantleAndPlaceTrap:

//                    //get all objects on the failed trap
//                    RSObject[] objectsOnTrap = Objects.getAt(taskToHandle.trapToHandle.locationOnGround);
//                    RSObject theFailedTrap = null;
//
//                    for (RSObject object : objectsOnTrap) {
//                        if (object.getID() == birdSnareFailedID) {
//                            theFailedTrap = object;
//                        }
//                    }
//
//                    if (theFailedTrap != null) {
//                        DynamicClicking.clickRSObject(theFailedTrap, 1);
//                    }

                    System.out.println("DISMANTLING AND PLACING");
                    Clicking.click("Dismantle", taskToHandle.trapToHandle.locationOnGround);
                    General.sleep(General.randomSD(700,1000,800,800));
                    while(doingAnimation() || Player.isMoving()) {
                        General.sleep(60,100);
                    }
                    General.sleep(General.randomSD(700,1000,800,800));


                    RSTile tile = taskToHandle.trapToHandle.locationOnGround;

                    System.out.println("MOVING BACK TO TRAP LOCATION");
                    placeDownTrap(new Location(tile.getX(), tile.getY(), 0));
                    System.out.println("PLACED DOWN NEW TRAP");
                    taskToHandle.trapToHandle.currentState = Trap.STATE.Wait;

                    break;


                case Dropping:

                    dropBirdLoot();

                    break;

            }//end of switch statements

            while(doingAnimation() || Player.isMoving()) {
                General.sleep(60,100);
            }

            //add a delay after doing an action to look human
            General.sleep(General.randomSD(500,800, 650, 650));

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

    public void placeDownTrap(Location location) {
        // Check if open location


        // player is not on top of the box trap tile
        if (Player.getPosition().getX() != location.tile.getX() || Player.getPosition().getY() != location.tile.getY()) {
            walkToTile(location.tile);

            // keeping sleeping until player stops moving
            while (Player.getPosition().getX() != location.tile.getX() || Player.getPosition().getY() != location.tile.getY()) {
                System.out.println("PLAYER IS MOVING");
                General.sleep(60, 120);
            }


            System.out.println("PLAYER REACHED TRAP TILE");
            //wait for a second or two after getting to the destination
            General.sleep(General.randomSD(700, 1200, 900, 900));
        }

        System.out.println("attemping to place tile");
        RSItem[] birdSnareInInventory = Inventory.find(birdSnareItemID);

        // box traps exist in inventory, place that down
        if (birdSnareInInventory.length > 0) {
            Clicking.click(birdSnareInInventory[0]);

            General.sleep(600, 1000);

            while (Player.getAnimation() > -1 ) {
                General.sleep(60, 120);
            }
        }
    }

    public void placeDownTrap() {
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
        if (Player.getPosition().getX() != openLocation.tile.getX() || Player.getPosition().getY() != openLocation.tile.getY()) {
            walkToTile(openLocation.tile);

            // keeping sleeping until player stops moving
            while (Player.getPosition().getX() != openLocation.tile.getX() || Player.getPosition().getY() != openLocation.tile.getY()) {
                System.out.println("PLAYER IS MOVING");
                General.sleep(60, 120);
            }

            System.out.println("PLAYER REACHED TRAP TILE");
            //wait for a second or two after getting to the destination
            General.sleep(General.randomSD(700, 1200, 900, 900));
        }

        System.out.println("attemping to place tile");
        RSItem[] birdSnareInInventory = Inventory.find(birdSnareItemID);

        // box traps exist in inventory, place that down
        if (birdSnareInInventory.length > 0) {
            Clicking.click(birdSnareInInventory[0]);
            trapsPlacedArray.add(new Trap(openLocation.tile.getPosition()));

            General.sleep(600, 1000);

            while (Player.getAnimation() > -1 ) {
                General.sleep(60, 120);
            }
        }
    }

    public void dropBirdLoot() {
        shiftDrop.shiftDrop(bonesID, birdMeatID);
    }

    public void hoverTrapAndClick(String option, Trap trap){
        // Add rng mouse clicks?
        Clicking.click(option, trap.locationOnGround);
    }

    public void walkToTile (RSTile tile) {
        if (tile.isOnScreen()) {
            System.out.println("CLICKED ON TRAP TILE TO MOVE TO IT PLZ");
            Clicking.click("Walk here", tile);
        }
    }

    // Function looks at the objects on the RSTile
    public Trap.STATE determineStateOfTrap(Trap trap){

        //INDIVIDUAL CHECK FOR GROUND ITEM BIRD SNARE, SINCE GROUND ITEMS ARE NOT RSOBJECTS
        RSGroundItem[] groundItems = GroundItems.getAt(trap.locationOnGround);

        for (RSGroundItem gItem: groundItems) {
            if (gItem.getID() == birdSnareItemID) {
                return Trap.STATE.Ground;
            }
        }

        // Get all the objects located on the trap
        RSObject[] objectsOnTrap = Objects.getAt(trap.locationOnGround);

        for(RSObject object : objectsOnTrap){
            if(object.getID() == placedBirdSnareID)
                return Trap.STATE.Wait;
            else if(object.getID() == birdSnareFailedID){
                System.out.println("TRAP AT LOCATION: " + trap.locationOnGround.getX() + ", " + trap.locationOnGround.getY() + " FAILED");
                return Trap.STATE.Failed;}
            else if(object.getID() == birdSnareSuccessID)
                return Trap.STATE.Success;
//            else if(object.getID() == birdSnareItemID)
//                return Trap.STATE.Ground;
//            else if(object.getID() == birdSnareIgnore)
//                break;
//            else if(object.getID() == birdSnareIgnore2)
//                break;
        }
        return Trap.STATE.NA;
    }

    public boolean isInventoryFull () {
        if (Inventory.getAll().length >= 26 ) {
            System.out.println("inventory is full");
            return true;
        }
        return false;
    }

    public boolean isThereSomethingToDo () {

        //inventory is full, so return true instantly
        if (isInventoryFull()) {
            return true;
        }

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

                int xCoord = trap.locationOnGround.getX();
                int yCoord = trap.locationOnGround.getY();

                //since the trap doesnt exist on that location, the location is not in use, so it is available for trap placement.
                for (Location location : locations) {
                    if (xCoord == location.tile.getX() && yCoord == location.tile.getY()) {
                        location.inUse = false;
                        break;
                    }
                }
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
                    //add check and place trap task to queue
                    tasksToDo.add(TASK.CheckAndPlaceTrap.setTrapToHandle(trap));
                    break;
                case Failed:
                    System.out.println("ADDING DISMANTLE AND PALCE TASK TO QUEUE");
                    tasksToDo.add(TASK.DismantleAndPlaceTrap.setTrapToHandle(trap));
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

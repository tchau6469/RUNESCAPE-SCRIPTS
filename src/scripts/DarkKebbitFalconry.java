package scripts;
import org.tribot.api.General;
//import org.tribot.api2007.*;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.NPCs;
import org.tribot.api2007.WebWalking;
import org.tribot.api2007.types.RSNPC;
import org.tribot.api2007.types.RSTile;
import org.tribot.script.Script;
import org.tribot.api.Timing;
import org.tribot.api.DynamicClicking;
import org.tribot.script.interfaces.MessageListening07;

import java.util.function.BooleanSupplier;

public class DarkKebbitFalconry extends Script implements MessageListening07 {

    public static void main(String[] args){}

    boolean RUNNING = true;
    final int minX = 180;
    final int maxX = 440;
    final int minY = 200;
    final int maxY = 300;

    enum TASK {Retrieving, Catching, Waiting, Dropping}

    final int spottedKebbit = 5531;
    final int spottedKebbitFur = 10125;
    final int spottedfalcon = 1342;
    final int darkKebbit = 5532;
    final int falcon = 1344;
    final int bones = 526;
    final int darkKebbitFur = 10115;

    boolean newMessage = false;

    String currentMessage = "";


    TASK currentTask = TASK.Catching;




    @Override
    public void serverMessageReceived(String serverMessage) {


        System.out.println("GETTING MESSAGEEEE");
        currentMessage = serverMessage;

        //do nothing if the message is the retrieve message
        if (currentMessage.contains("retrieve")) {
            System.out.println("RETRIEVE MESSAGE ACQUIRED. IGNORE IT");
            return;
        }

        if (currentMessage.contains("quick")) {
            System.out.println("CLICK ON KEBBIT WITHOUT FALCON MESSAGE, IGNORE IT");
            return;
        }

        if (currentMessage.contains("Woodcutting")) {
            System.out.println("CLICKED ON OAK TREE, IGNORE IT");
            return;
        }

        if (currentMessage.contains("57")) {
            System.out.println("CLICKED ON DARK KEBBIT, IGNORE IT");
            return;
        }

        //clicked on tree while recieving, end script
        if (currentMessage.contains("axe") && currentTask == TASK.Retrieving) {
            System.out.println("CLICKED ON TREE WHILE RECEIVING.. ENDING");
            RUNNING = false;
            return;
        }

        newMessage = true;

    }

    //method to do whatever with the new server message
    public void decipherMessage() {

        System.out.println("DECIPHERING MESSAGE");


        if (currentMessage.contains("successfully")) {

            System.out.println("CAUGHT THE BITCH, SWITCHING TO RETRIEVING MODE");


            currentTask = TASK.Retrieving;
        }
        //if the message contains misses(you falcon failed) or
        //if  message contains retrieve (you collect the kebbit loot)
        //then we are back in catching mode (throwing the falcon)
        else if (currentMessage.contains("misses") || currentMessage.contains("axe")) {

            System.out.println("FALCON MISSED THE KEBBIT, SWITCHING BACK TO CATCHING MODE");

            currentTask = TASK.Catching;

        }
        newMessage = false;

    }//end of decipherMessage()

    public void run() {
        while (RUNNING) {

            switch(currentTask) {

                case Dropping:
                    //drop inventory
                    dropSpottedLoot();

                    //make the currentTask to be catching cuz we ready to catch
                    currentTask = TASK.Catching;

                    //put bot to sleep for around 1-3 seconds, cant be too fast
                    General.sleep(General.randomSD(1000, 3000, 1200, 1200));

                    break;
                //currently on catching task, so we will find a kebbit and click it
                case Catching:
                    RSNPC nearbyKebbit = findSpottedKebbit();

                    //there is no kebbit
                    while (nearbyKebbit == null) {
                        RSTile hotSpotTile = new RSTile (General.random(minX, maxX), General.random(minY, maxY));

                        WebWalking.walkTo(hotSpotTile);

                        General.sleep(General.randomSD(5000, 7000, 6000, 6000));

                        //try to find kebbit again
                        nearbyKebbit = findSpottedKebbitAgain();
                    }

                    catchKebbit(nearbyKebbit);

                    System.out.println("THREW FALCON, GOING INTO WAITING MODE");
                    currentTask = TASK.Waiting;
                    break;


                //waiting for message after clicking on kebbit
                case Waiting:
                    //wait for new server message to appear
                    Timing.waitCondition(() -> {General.sleep(30, 50); return newMessage;} , General.randomSD(4000, 7000, 5000, 5000));


                        //decipher the new server messsage
                        decipherMessage();

                    General.sleep(General.randomSD(500, 1000, 500, 500));

                    break;
                case Retrieving:
                    retrieveFalcon();

                    //gives time for falcon pickup
                    General.sleep(General.randomSD(2000, 2800, 2100, 2100));

                    System.out.println("COLLECTED FALCON, GOING INTO CATCHING MODE OR DROPPING");

                    //if inventory has filled with 27 items, it is considered full cuz u need 2 spots for kebbit loot
                    if (Inventory.getAll().length >= 27 ) {
                        System.out.println("inventory is full");
                        currentTask = TASK.Dropping;
                        General.sleep(General.randomSD(1000, 2000, 1300, 1000));
                    }

                    else {
                        currentTask = TASK.Catching;
                    }

                    break;

            }

        }//end of while (true)
    }//end of run()

    public void retrieveFalcon() {
        RSNPC[] theFalcon = NPCs.find(falcon, spottedfalcon);

        if (theFalcon.length > 0 ) {
            System.out.println("Found falcon");
            if (theFalcon[0].isClickable()) {
                System.out.println("Clicking falcon");
            }

            else {
                System.out.println("Adjusting camera to falcon");
                theFalcon[0].adjustCameraTo();
                System.out.println("clicking falcon");
            }
            DynamicClicking.clickRSNPC(theFalcon[0], 1);

            int result = Timing.waitCrosshair(General.randomSD(100, 300, 200, 200));

            //result < 2 means it is either 0 or 1, meaning it missed the click
            if (result < 2) {
                System.out.println("BOT MISSED THE FALCON CLICK, GONNA CLICK AGAIN");
                General.sleep(General.randomSD(100, 300, 200, 200));
                retrieveFalcon();
            }
            else {
                System.out.println("SUCCESSFUL RETRIEVE FALCON CLICK, PLAYER IS MOVING");
            }

        }
    }


    public void catchKebbit(RSNPC kebbit) {
        if (!kebbit.isClickable()) {
            kebbit.adjustCameraTo();
        }

        DynamicClicking.clickRSNPC(kebbit, 1);

        int result = Timing.waitCrosshair(General.randomSD(100, 300, 200, 1));

        if (result < 2) {
            System.out.println("BOT MISSED THE KEBBIT CLICK, GONNA CLICK AGAIN");
            General.sleep(General.randomSD(100, 300, 200, 1));
            catchKebbit(kebbit);
        }
        else {
            System.out.println("SUCCESSFUL KEBBIT CLICK");
        }

    }//end of catchKebbit()


    //REPLACE WHEN IMPLEMENTING GENERIC KEBBIT
    //find nearby spotted kebbits.
    //if found, return the nearest spotted kebbit
    //if none found, return null
    public RSNPC findSpottedKebbit () {
        RSNPC[] nearbySpottedKebbits = NPCs.findNearest(darkKebbit, spottedKebbit);

        //no kebbit found
        if (nearbySpottedKebbits.length < 1) {
            System.out.println("no spotted kebbits nearby");
            return null;
        }



        //only 1 kebbit nearby, so returning the only one
        return nearbySpottedKebbits[0];

    } //end of findSpottedKebbit()

    public RSNPC findSpottedKebbitAgain() {
        RSNPC[] nearbySpottedKebbits = NPCs.findNearest(darkKebbit, spottedKebbit);

        //no kebbit found
        if (nearbySpottedKebbits.length < 1) {
            System.out.println("no spotted kebbits nearby");
            return null;
        }

        //returns one out of two nearby kebbits
        if (nearbySpottedKebbits.length > 1) {
            System.out.println("returning kebbit");
            return nearbySpottedKebbits[General.random(0, 1)];
        }



        //only 1 kebbit nearby, so returning the only one
        return nearbySpottedKebbits[0];

    } //end of findSpottedKebbit()



    //method to drop spotted kebbit fur
    public void dropSpottedLoot() {
        shiftDrop.shiftDrop(bones, darkKebbitFur, spottedKebbitFur);
    }


}

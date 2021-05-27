package scripts;

import org.tribot.api.General;
import org.tribot.api2007.GroundItems;
import org.tribot.api2007.Objects;
import org.tribot.api2007.types.RSGroundItem;
import org.tribot.api2007.types.RSObject;
import org.tribot.api2007.types.RSTile;
import org.tribot.script.Script;
import sun.java2d.loops.FillRect;

public class TESTING_SCRIPT extends Script {


    public void run() {
        while(true) {
            RSGroundItem[] groundItems = GroundItems.getAt(new RSTile(2523, 2935,0));

            if (groundItems.length < 1) {
                System.out.println("NO GROUND ITEMS DETECTED");
            }

            for (RSGroundItem item : groundItems) {
                System.out.println(item.getDefinition().getID());
//                if (== 10006) {
//                    System.out.println("FOUND TRAP AS GROUND ITEM BOY");
//                }
            }


            RSObject[] objectsOnTrap = Objects.getAt(new RSTile(2523, 2935,0));
            for (RSObject object : objectsOnTrap) {

                if (object.getID() == 9345) {
                    System.out.println("FOUND TRAP AS INTERACTIVE OBJECT");
                }
                else {System.out.println("trap not found");}
            }

            General.sleep(1000);

        }
    }
}

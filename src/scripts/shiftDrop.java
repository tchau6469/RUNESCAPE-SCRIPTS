package scripts;

import java.awt.event.KeyEvent;
import java.util.ArrayList;

import org.tribot.api.Clicking;
import org.tribot.api.General;
import org.tribot.api.input.Keyboard;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.types.RSItem;

public class shiftDrop {
    public static void shiftDrop(int... ids) {
        RSItem[] itemsToDrop;
        ArrayList<RSItem> itemsToDropOrdered = new ArrayList<>();

        //ids sent in was empty, so drop whole inventory
        if (ids.length < 1) {
            itemsToDrop = Inventory.getAll();
        }
        //find the items in the inventory that match the ids to drop
        else {
            itemsToDrop = Inventory.find(ids);
        }

        //order that shit
        for (int i = 0; i < 4; i++) {
            for (RSItem item : itemsToDrop) {
                if (item.getIndex() >= (0 + 8 * i) && item.getIndex() <= (3 + 8 * i)) {
                    itemsToDropOrdered.add(item);
                }
            }
            for (int j = itemsToDrop.length - 1; j >= 0; j--) {
                if (itemsToDrop[j].getIndex() >= (4 + 8 * i) && itemsToDrop[j].getIndex() <= (7 + 8 * i)) {
                    itemsToDropOrdered.add(itemsToDrop[j]);
                }
            }
        }

        //drop items
        if (Inventory.open()) {
            Keyboard.sendPress(KeyEvent.CHAR_UNDEFINED, KeyEvent.VK_SHIFT);

            for (RSItem item : itemsToDropOrdered) {

                Clicking.click(item);
                General.sleep(General.randomSD(200, 500, 300, 300));
            }


            Keyboard.sendRelease(KeyEvent.CHAR_UNDEFINED, KeyEvent.VK_SHIFT);
        }


    }//end of shiftDrop(int... ids)

    public static void shiftDropAll() {
        shiftDrop();
    }
}

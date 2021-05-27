package scripts;

import org.tribot.api.General;
import org.tribot.script.Script;
import sun.java2d.loops.FillRect;

public class BlackLizard extends Script {

    public static void main (String[] args) throws InterruptedException {
        while(true) {
            long sleepPeriod = General.random(500, 1000);
            System.out.println(sleepPeriod);
            Thread.sleep(1000);
        }

    }
    public void run() {
        System.out.println("hi");
    }
}

package javalibs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Sean Grimes, sean@seanpgrimes.com
 * @since 6/7/15
 */

class TestSpace implements Functor{

    public void execute(){
        System.out.println("Execute");
    }

    public static void testThis(){
        System.out.println("testThis");
    }

    public static void main(String[] args) throws Exception {
        TSL log = TSL.get();
        if(Validation.Int("a"))
            log.info("is an int");
        else 
            log.get().info("Not an int");

        Map<String, Functor> map = new HashMap<>();
        map.put("test", () -> {
            testThis();
        });

        Functor method = map.get("test");
        method.execute();

        log.info("CPU Core Count: " + SysHelper.get().physicalCPUCoreCount());
        log.info("Avail CPU: " + Runtime.getRuntime().availableProcessors());
        log.shutDown();
    }

    public static void notMuchOfAFunction(){
        TSL.get().autoLog("Message");
    }

}

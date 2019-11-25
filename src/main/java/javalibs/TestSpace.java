package javalibs;

import java.util.HashMap;
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
        notMuchOfAFunction();
        TSL.get().die();

        Map<String, Functor> map = new HashMap<>();
        map.put("test", () -> {
            testThis();
        });

        Functor method = map.get("test");
        method.execute();



    }

    public static void notMuchOfAFunction(){
        TSL.get().autoLog("Message");
    }

}

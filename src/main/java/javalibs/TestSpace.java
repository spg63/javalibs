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
        notMuchOfAFunction();
        //TSL.get().die();

        Map<String, Functor> map = new HashMap<>();
        map.put("test", () -> {
            testThis();
        });

        Functor method = map.get("test");
        method.execute();

        List<Integer> rands = NumUtils.RandomizedList0toExclusiveNWithoutRepeats(100);
        for(int i = 0; i < rands.size(); ++i) {
            System.out.println("i (" + i + "): " + rands.get(i));
        }

        TSL.get().shutDown();
    }

    public static void notMuchOfAFunction(){
        TSL.get().autoLog("Message");
    }

}

package javalibs;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Sean Grimes, sean@seanpgrimes.com
 * @since 6/7/15
 * NOTE: This class is *NOT* in a usable state
 */
public class FunctionMap{
    private Map<String, Functor> map_of_functions = null;

    public FunctionMap(){
        this.map_of_functions = new HashMap<>();
    }
/*
    public void addFunction(String function_name, javalibs.Functor function){
        this.map_of_functions.put(function_name, () -> {
            function();
        });
    }
*/
}

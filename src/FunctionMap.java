import java.util.HashMap;
import java.util.Map;

/**
 * @author Sean Grimes, spg63@cs.drexel.edu
 * @since 6/7/15
 */
public class FunctionMap{
    private Map<String, Functor> map_of_functions = null;

    public FunctionMap(){
        this.map_of_functions = new HashMap<>();
    }
/*
    public void addFunction(String function_name, Functor function){
        this.map_of_functions.put(function_name, () -> {
            function();
        });
    }
*/
}

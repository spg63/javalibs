/**
 * @author Sean Grimes, spg63@cs.drexel.edu
 * @since 6/6/15
 */
@SuppressWarnings("unused")
public class Validation{
    public static boolean Int(String number){
        try{
            //noinspection ResultOfMethodCallIgnored
            Integer.parseInt(number);
            return true;
        }
        catch(Exception e){
            return false;
        }
    }

    public static boolean Double(String number){
        try{
            //noinspection ResultOfMethodCallIgnored
            Double.parseDouble(number);
            return true;
        }
        catch(Exception e){
            return false;
        }
    }

    public static boolean between(int num, int start, int end){
        return num >= start && num <= end;
    }

}

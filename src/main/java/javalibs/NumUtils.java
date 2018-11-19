package javalibs;

public class NumUtils {
    public static double getDouble(String aNumberIHope) {
        double val = 0;
        try {
            val = Double.parseDouble(aNumberIHope);
        }
        catch(NumberFormatException e){
            TSL.get().logAndKill(e);
        }
        return val;
    }

    public static long getLong(String aNumberIHope) {
        long val = 0;
        try{
            val = Long.parseLong(aNumberIHope);
        }
        catch(NumberFormatException e){
            TSL.get().logAndKill(e);
        }
        return val;
    }
}

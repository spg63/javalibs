package javalibs;

import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("WeakerAccess")
public class NumUtils {
    public static double getDoubleFromStr(String aNumberIHope) {
        double val = 0;
        try {
            val = Double.parseDouble(aNumberIHope);
        }
        catch(NumberFormatException e){
            TSL.get().logAndKill(e);
        }
        return val;
    }

    public static long getLongFromStr(String aNumberIHope) {
        long val = 0;
        try{
            val = Long.parseLong(aNumberIHope);
        }
        catch(NumberFormatException e){
            TSL.get().logAndKill(e);
        }
        return val;
    }

    public static int randomBoundedInclusiveInt(int start, int end) {
        return ThreadLocalRandom.current().nextInt(start, end + 1);
    }


    public static double normalizeBetweenZeroOne(double min, double max, double val) {
        return (val - min) / (max - min);
    }
}

package javalibs;

/**
 * Copyright (javalibs) 2019 Sean Grimes. All rights reserved.
 * @author Sean Grimes, sean@seanpgrimes.com
 * @since 1/31/17
 * License: MIT License
 */

import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("WeakerAccess")
public class NumUtils {
    public static double getDoubleFromStr(String aNumberIHope) {
        double val = 0;
        try {
            val = Double.parseDouble(aNumberIHope);
        }
        catch(NumberFormatException e){
            TSL.get().die(e);
        }
        return val;
    }

    public static long getLongFromStr(String aNumberIHope) {
        long val = 0;
        try{
            val = Long.parseLong(aNumberIHope);
        }
        catch(NumberFormatException e){
            TSL.get().die(e);
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

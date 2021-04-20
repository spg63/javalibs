package javalibs;

/**
 * Copyright (javalibs) 2019 Sean Grimes. All rights reserved.
 * @author Sean Grimes, sean@seanpgrimes.com
 * @since 1/31/17
 * License: MIT License
 */

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("WeakerAccess")
public class NumUtils {
    /**
     * Returns null on fail to parse
     * @param aNumberIHope
     * @return The double or null
     */
    public static Double getDoubleFromStr(String aNumberIHope) {
        double val = 0;
        try {
            val = Double.parseDouble(aNumberIHope);
        }
        catch(NumberFormatException e){
            //TSL.get().exception(e);
            //TSL.get().autoLog("aNumberIHope: " + aNumberIHope);
            return null;
        }
        return val;
    }

    /**
     * Returns null on fail to parse
     * @param aNumberIHope
     * @return The long or null on failure
     */
    public static Long getLongFromStr(String aNumberIHope) {
        long val = 0;
        try{
            val = Long.parseLong(aNumberIHope);
        }
        catch(NumberFormatException e){
            //TSL.get().exception(e);
            //TSL.get().autoLog("aNumberIHope: " + aNumberIHope);
            return null;
        }
        return val;
    }

    public static int randomBoundedInclusiveInt(int start, int end) {
        return ThreadLocalRandom.current().nextInt(start, end + 1);
    }

    public static double randomBoundedExclusiveDouble(double start, double end) {
        return ThreadLocalRandom.current().nextDouble(start, end);
    }

    public static List<Integer> RandomizedList0toExclusiveNWithoutRepeats(int exclusiveEnd) {
        List<Integer> numList = new ArrayList();
        List<Integer> randList = new ArrayList();
        for(int i = 0; i < exclusiveEnd; ++i) numList.add(i);
        int end = numList.size() - 1;
        while(end >= 0) {
            int rand = end > 0 ? ThreadLocalRandom.current().nextInt(end) : 0;
            // Get a random element from the numbers list
            randList.add(numList.get(rand));
            // Overwrite the element with the end of the numbers list and reduce list size
            numList.set(rand, numList.get(end));
            --end;
        }
        return randList;
    }

    public static double normalizeBetweenZeroOne(double min, double max, double val) {
        return (val - min) / (max - min);
    }
}

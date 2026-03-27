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
     * Parse a double from a string
     * @param aNumberIHope The string to parse
     * @return The double value, or null if parsing fails
     */
    public static Double getDoubleFromStr(String aNumberIHope) {
        try {
            return Double.parseDouble(aNumberIHope);
        }
        catch(NumberFormatException e){
            return null;
        }
    }

    /**
     * Parse a long from a string
     * @param aNumberIHope The string to parse
     * @return The long value, or null if parsing fails
     */
    public static Long getLongFromStr(String aNumberIHope) {
        try {
            return Long.parseLong(aNumberIHope);
        }
        catch(NumberFormatException e){
            return null;
        }
    }

    public static int randomBoundedInclusiveInt(int start, int end) {
        return ThreadLocalRandom.current().nextInt(start, end + 1);
    }

    public static double randomBoundedExclusiveDouble(double start, double end) {
        return ThreadLocalRandom.current().nextDouble(start, end);
    }

    public static List<Integer> randomizedList0ToExclusiveNWithoutRepeats(int exclusiveEnd) {
        List<Integer> numList = new ArrayList<>();
        List<Integer> randList = new ArrayList<>();
        for(int i = 0; i < exclusiveEnd; ++i) numList.add(i);
        int end = numList.size() - 1;
        while(end >= 0) {
            int rand = end > 0 ? ThreadLocalRandom.current().nextInt(end) : 0;
            randList.add(numList.get(rand));
            numList.set(rand, numList.get(end));
            --end;
        }
        return randList;
    }

    /**
     * Normalize a value to the range [0, 1]
     * @param min The minimum value of the range
     * @param max The maximum value of the range
     * @param val The value to normalize
     * @return The normalized value between 0 and 1
     * @throws IllegalArgumentException if min == max
     */
    public static double normalizeBetweenZeroOne(double min, double max, double val) {
        if(Double.compare(min, max) == 0)
            throw new IllegalArgumentException("min and max must not be equal");
        return (val - min) / (max - min);
    }
}

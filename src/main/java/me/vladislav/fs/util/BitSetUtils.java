package me.vladislav.fs.util;

import java.util.BitSet;

public class BitSetUtils {

    public static boolean isAllTrue(BitSet bitSet) {
        return bitSet.cardinality() == bitSet.size();
    }
}

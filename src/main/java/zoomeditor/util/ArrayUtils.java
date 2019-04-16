package main.java.zoomeditor.util;

import java.util.Arrays;

public class ArrayUtils {

    /**
     * Creates a new array, filled with specified bytes.
     *
     * @param size size of new array
     * @param b    byte
     * @return a new array
     */
    public static byte[] makeAndFillArray(int size, byte b) {
        byte[] byteArray = new byte[size];
        Arrays.fill(byteArray, b);
        return byteArray;
    }

    /**
     * Copies the part of the specified array into a new array.
     * Almost the same as Arrays.copyOfRange(), but third parameter is size.
     *
     * @param bytes    the array from which a range is to be copied
     * @param position the initial index of the range to be copied, inclusive
     * @param size     number of bytes to copy
     * @return a new array
     */
    public static byte[] copyPart(byte[] bytes, int position, int size) {
        return Arrays.copyOfRange(bytes, position, position + size);
    }

}

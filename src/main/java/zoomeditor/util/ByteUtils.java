package main.java.zoomeditor.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ByteUtils {

    /**
     * Finds the first occurrence of the pattern in the text. Knuth-Morris-Pratt algorithm for pattern matching is used.
     *
     * @see <a href="https://stackoverflow.com/questions/1507780/searching-for-a-sequence-of-bytes-in-a-binary-file-with-java">Stackoverflow</a>
     */
    public static int indexOf(byte[] data, byte[] pattern, int offset) {
        if (data.length == 0) return -1;

        int[] failure = computeFailure(pattern);
        int j = 0;

        for (int i = offset; i < data.length; i++) {
            while (j > 0 && pattern[j] != data[i]) {
                j = failure[j - 1];
            }
            if (pattern[j] == data[i]) {
                j++;
            }
            if (j == pattern.length) {
                return i - pattern.length + 1;
            }
        }
        return -1;
    }

    /**
     * Computes the failure function using a boot-strapping process, where the pattern is matched against itself.
     *
     * @see <a href="https://stackoverflow.com/questions/1507780/searching-for-a-sequence-of-bytes-in-a-binary-file-with-java">Stackoverflow</a>
     */
    private static int[] computeFailure(byte[] pattern) {
        int[] failure = new int[pattern.length];

        int j = 0;
        for (int i = 1; i < pattern.length; i++) {
            while (j > 0 && pattern[j] != pattern[i]) {
                j = failure[j - 1];
            }
            if (pattern[j] == pattern[i]) {
                j++;
            }
            failure[i] = j;
        }

        return failure;
    }

    /**
     * Converts hex string to byte array
     *
     * @see <a href="https://stackoverflow.com/questions/11208479/how-do-i-initialize-a-byte-array-in-java">Stackoverflow</a>
     */
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * Converts byte[2] to short.
     * Since Java does not have unsigned short primitive type, the value is converted to int.
     */
    public static int bytesToUnsignedShortAsInt(byte[] bytes) {
        if (bytes == null || bytes.length != 2) {
            throw new ArithmeticException("invalid byte input");
        }
        return Short.toUnsignedInt(ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getShort());
    }

    /**
     * Converts byte[4] to int.
     * Since Java does not have unsigned int primitive type, if result is bigger than 2^31, then exception is thrown.
     * But 2^31 is enough for byte array operations.
     */
    public static int bytesToInt(byte[] bytes) {
        if (bytes == null || bytes.length != 4) {
            throw new NumberFormatException("byte to int conversion error: invalid input");
        }
        int result = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getInt();
        if (result < 0) {
            throw new NumberFormatException("byte to int conversion error: int result is too big; use long!");
        }
        //return Math.toIntExact(ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getLong());
        return result;
    }

    /**
     * Converts short to byte[].
     */
    public static byte[] shortToBytes(short value) {
        return ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(value).array();
    }

    /**
     * Converts int to byte[].
     */
    public static byte[] intToBytes(int value) {
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array();
    }

    /**
     * Converts byte[] to String of hex values, separated by "|".
     * USE ONLY FOR DEBUG or LOGGING!
     */
    public static String bytesToHexString(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%x|", b));
        }
        return sb.toString();
    }

}

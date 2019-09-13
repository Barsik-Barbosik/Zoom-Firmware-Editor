package main.java.zoomeditor.model;

import main.java.zoomeditor.util.ArrayUtils;
import main.java.zoomeditor.util.ByteUtils;

public class FlstSeqZDT {
    public static final String FILE_NAME = "FLST_SEQ.ZDT";
    public static final int FILE_SIZE = 4108;
    public static final byte[] DEFAULT_TYPE_ORDER = ByteUtils.hexStringToByteArray("000102030C0D14160405060708090B");
    public static final byte[] OPENING_LINE = ByteUtils.hexStringToByteArray("3E3E3E00000000000000000000");
    public static final String EMPTY_LINE = new String(ArrayUtils.makeAndFillArray(13, (byte) 0x00));
    public static final byte[] ENDING_LINE = ByteUtils.hexStringToByteArray("3C3C3C00000000000000000000");
    public static final int TYPE_BYTE_POS_IN_SEQ = 4;
    public static final int TYPE_BYTE_POS_IN_ZDL = 60;
}

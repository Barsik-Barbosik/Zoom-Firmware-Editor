package main.java.zoomeditor.model;

import main.java.zoomeditor.util.ByteUtils;

public class FlstSeqZDT {
    public static final byte[] TYPE_ORDER = ByteUtils.hexStringToByteArray("000102030405060708090B0C");
    public static final byte[] OPENING_LINE = ByteUtils.hexStringToByteArray("3E3E3E00000000000000000000");
    public static final byte[] EMPTY_LINE = ByteUtils.hexStringToByteArray("00000000000000000000000000");
    public static final byte[] ENDING_LINE = ByteUtils.hexStringToByteArray("3C3C3C00000000000000000000");
    public static final int TYPE_BYTE_POS_IN_SEQ = 4;
    public static final int TYPE_BYTE_POS_IN_ZDL = 60;
}

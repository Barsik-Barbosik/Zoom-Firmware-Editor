package main.java.zoomeditor.model;

import main.java.zoomeditor.util.ByteUtils;

public class FlstSeqZDT {
    public static final int LINE_WIDTH = 13; // 26
    public static final byte[] TYPE_OPENING = ByteUtils.hexStringToByteArray("3E3E3E00000000000000000000");
    public static final byte[] TYPE_ENDING = ByteUtils.hexStringToByteArray("3C3C3C00000000000000000000");
    public static final int TYPE_OFFSET = 5; // 4
    public static final int ZDL_OFFSET = 60;
    // TODO: replace with 0 to 12?
    public static final byte[] TYPE_ORDER = ByteUtils.hexStringToByteArray("000102030405060708090B0C");
}

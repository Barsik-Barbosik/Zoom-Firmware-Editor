package main.java.zoomeditor.model;

import main.java.zoomeditor.util.ArrayUtils;
import main.java.zoomeditor.util.ByteUtils;

public class Patch {
    public static final int ADDR_OFFSET = 0;
    public static final int ADDR_SIZE = 2;
    public static final int SIZE_OFFSET = 4;
    public static final int SIZE_SIZE = 4;
    public static final int FILENAME_OFFSET = 8;
    public static final int FILENAME_SIZE = 12;
    private static final byte[] ON_OFF_PATTERN = "OnOff".getBytes();
    private static final byte[] NAME_END_PATTERN = ArrayUtils.makeAndFillArray(4, (byte) 0xFF);
    private static final int NAME_SIZE = 12; // actually maximum used name length is 9

    private String fileName;
    private String name;
    private int address; // patch's first block address
    private int size;
    private byte[] fileTableItem;
    private byte[] content;
    private byte type;

    public String extractFileNameFromFileTableItem() {
        if (fileTableItem == null || fileTableItem.length == 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int pos = FILENAME_OFFSET; pos < FILENAME_OFFSET + FILENAME_SIZE; pos++) {
            if (fileTableItem[pos] == (byte) 0x00) {
                break;
            }
            sb.append((char) fileTableItem[pos]);
        }
        return sb.toString();
    }

    public String extractNameFromContent() {
        if (content == null || content.length == 0) {
            return null;
        }
        int onOffPos = ByteUtils.indexOf(content, ON_OFF_PATTERN, 0); // offset could be increased for optimization
        if (onOffPos == -1) {
            return null; // raw-files are nameless
        }
        int nameEndPos = ByteUtils.indexOf(content, NAME_END_PATTERN, onOffPos);
        return new String(ArrayUtils.copyPart(content, nameEndPos - NAME_SIZE, NAME_SIZE)).trim();
    }

    public byte extractTypeFromContent() {
        if (content == null || content.length == 0) {
            return (byte) 0xFF; // null
        }
        return content[FlstSeqZDT.TYPE_BYTE_POS_IN_ZDL];
    }

    public byte[] getAddressBytes() {
        if (getFileTableItem() == null || getFileTableItem().length == 0) {
            return null;
        }
        return ArrayUtils.copyPart(getFileTableItem(), ADDR_OFFSET, ADDR_SIZE);
    }

    public byte[] getSizeBytes() {
        if (getFileTableItem() == null || getFileTableItem().length == 0) {
            return null;
        }
        return ArrayUtils.copyPart(getFileTableItem(), SIZE_OFFSET, SIZE_SIZE);
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAddress() {
        return address;
    }

    public void setAddress(int address) {
        this.address = address;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public byte[] getFileTableItem() {
        return fileTableItem;
    }

    public void setFileTableItem(byte[] fileTableItem) {
        this.fileTableItem = fileTableItem;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "===[ fileName: " + getFileName() + " ]===" +
                "\nname: " + getName() +
                "\nfile table item: " + ByteUtils.bytesToHexString(getFileTableItem()) +
                "\ncontent: " + ByteUtils.bytesToHexString(getContent()) +
                "\naddress: " + ByteUtils.bytesToHexString(getAddressBytes()) +
                "\nint address: " + getAddress() +
                "\nsize: " + ByteUtils.bytesToHexString(getSizeBytes()) +
                "\nint size: " + getSize();
    }

}

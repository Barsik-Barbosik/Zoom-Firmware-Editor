package main.java.zoomeditor.model;

public class FileTable {
    public static final int SYSTEM_DATA_SIZE = 8;
    public static final int ITEM_SIZE = 32;

    private int fileTablePosition; // position of the main file table
    // private ArrayList<byte[]> systemDataList; // list, that contains system data (first 8 bytes) of all 4 tables

    public int getFileTablePosition() {
        return fileTablePosition;
    }

    public void setFileTablePosition(int fileTablePosition) {
        this.fileTablePosition = fileTablePosition;
    }

}

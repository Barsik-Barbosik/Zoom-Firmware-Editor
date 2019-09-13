package main.java.zoomeditor.model;

import main.java.zoomeditor.enums.PedalSeries;
import main.java.zoomeditor.util.ByteUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Firmware {
    public static final byte[] BIN_START_PATTERN = ByteUtils.hexStringToByteArray("55AA00010400");
    public static final int BIN_BLOCKS_COUNT_OFFSET = 8;
    public static final int BIN_BLOCKS_COUNT_SIZE = 2;
    public static final int SYS_BLOCKS_COUNT = 11;
    public static final int FIRST_DATA_BLOCK = 10; // WHY 10!?
    public static final int BLOCK_SIZE = 4096;
    public static final int BLOCK_PREV_ADDR_OFFSET = 0; // previous address
    public static final int BLOCK_PREV_ADDR_SIZE = 2;
    public static final int BLOCK_NEXT_ADDR_OFFSET = 2; // next address
    public static final int BLOCK_NEXT_ADDR_SIZE = 2;
    public static final int BLOCK_SIZE_OFFSET = 4;
    public static final int BLOCK_SIZE_SIZE = 2;
    public static final int BLOCK_INFO_SIZE = BLOCK_PREV_ADDR_SIZE + BLOCK_NEXT_ADDR_SIZE + BLOCK_SIZE_SIZE;
    public static final List<String> EXCLUDE_FILENAMES = Collections.unmodifiableList(
            Arrays.asList("FLST_SEQ.ZDT", "FLST_SEQ.ZT2")); // will be excluded from the file table

    private PedalSeries pedalSeries;
    private final File firmwareFile;
    private FileTable fileTable;
    private int binStartPosition;
    private int binBlocksCount;

    private byte[] systemBytes; // 11 blocks: 3 first blocks and 4*2 "file table" blocks
    private byte[] dataBytes; // NB! First data block contains part of last file table
    private String[] blocks;
    private ArrayList<Effect> effects;

    public Firmware(File firmwareFile) {
        this.firmwareFile = firmwareFile;
        binStartPosition = -1;
        binBlocksCount = -1;
        systemBytes = null;
        dataBytes = null;
        blocks = null;
        effects = null;
    }

    public PedalSeries getPedalSeries() {
        return pedalSeries;
    }

    public void setPedalSeries(PedalSeries pedalSeries) {
        this.pedalSeries = pedalSeries;
    }

    public File getFirmwareFile() {
        return firmwareFile;
    }

    public FileTable getFileTable() {
        return fileTable;
    }

    public void setFileTable(FileTable fileTable) {
        this.fileTable = fileTable;
    }

    public int getBinStartPosition() {
        return binStartPosition;
    }

    public void setBinStartPosition(int binStartPosition) {
        this.binStartPosition = binStartPosition;
    }

    public int getBinBlocksCount() {
        return binBlocksCount;
    }

    public void setBinBlocksCount(int binBlocksCount) {
        this.binBlocksCount = binBlocksCount;
    }

    public byte[] getSystemBytes() {
        return systemBytes;
    }

    public void setSystemBytes(byte[] systemBytes) {
        this.systemBytes = systemBytes;
    }

    public byte[] getDataBytes() {
        return dataBytes;
    }

    public void setDataBytes(byte[] dataBytes) {
        this.dataBytes = dataBytes;
    }

    public String[] getBlocks() {
        return blocks;
    }

    public void setBlocks(String[] blocks) {
        this.blocks = blocks;
    }

    public ArrayList<Effect> getEffects() {
        return effects;
    }

    public void setEffects(ArrayList<Effect> effects) {
        this.effects = effects;
    }
}

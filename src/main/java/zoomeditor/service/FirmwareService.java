package main.java.zoomeditor.service;

import main.java.ZoomFirmwareEditor;
import main.java.zoomeditor.enums.PedalSeries;
import main.java.zoomeditor.model.Effect;
import main.java.zoomeditor.model.FileTable;
import main.java.zoomeditor.model.Firmware;
import main.java.zoomeditor.model.FlstSeqZDT;
import main.java.zoomeditor.util.ArrayUtils;
import main.java.zoomeditor.util.ByteUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class FirmwareService {
    private static volatile FirmwareService instance = null;
    private static final Logger log = Logger.getLogger(FirmwareService.class.getName());

    private FirmwareService() {
    }

    public static FirmwareService getInstance() {
        if (instance == null) {
            synchronized (FirmwareService.class) {
                if (instance == null) {
                    instance = new FirmwareService();
                }
            }
        }
        return instance;
    }

    /**
     * Creates and prepares the the firmware object.
     *
     * @param firmwareFile firmware file
     * @return firmware object
     */
    public Firmware initFirmware(File firmwareFile) {
        log.info("Init firmware: " + firmwareFile);
        Firmware firm = new Firmware(firmwareFile);
        if (firmwareFile == null) {
            throw new RuntimeException("Firmware file is not selected!");
        }

        try {
            byte[] allBytes = Files.readAllBytes(firm.getFirmwareFile().toPath());
            int binStartPosition = ByteUtils.indexOf(allBytes, Firmware.BIN_START_PATTERN, 0);
            if (binStartPosition == -1) {
                log.severe("BIN is not found!");
                throw new RuntimeException("BIN is not found!");
            }
            firm.setBinStartPosition(binStartPosition);
            firm.setBinBlocksCount(ByteUtils.bytesToUnsignedShortAsInt(ArrayUtils.copyPart(allBytes,
                    firm.getBinStartPosition() + Firmware.BIN_BLOCKS_COUNT_OFFSET, Firmware.BIN_BLOCKS_COUNT_SIZE)));
            firm.setSystemBytes(ArrayUtils.copyPart(allBytes,
                    firm.getBinStartPosition(), Firmware.BLOCK_SIZE * Firmware.SYS_BLOCKS_COUNT));
            firm.setDataBytes(ArrayUtils.copyPart(allBytes,
                    firm.getBinStartPosition() + Firmware.BLOCK_SIZE * Firmware.FIRST_DATA_BLOCK,
                    Firmware.BLOCK_SIZE * (firm.getBinBlocksCount() - Firmware.FIRST_DATA_BLOCK)));
            firm.setPedalSeries(detectPedalSeries(allBytes));
            log.info("Pedal series: " + firm.getPedalSeries());
            log.info("BIN blocks count: " + firm.getBinBlocksCount()
                    + ", BIN size: " + (Firmware.BLOCK_SIZE * firm.getBinBlocksCount()) + " bytes");
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(e);
        }

        boolean isSuccess = FileTableService.getInstance().fillFileTable(firm);
        if (!isSuccess) {
            log.severe("File table is not found!");
            throw new RuntimeException("File table is not found!");
        }

        // TODO: SORT FILE TABLE BY SEQUENCE & SEQUENCE DEFAULT_TYPE_ORDER!

        FileTableService.getInstance().fillEffectsAndBlocks(firm);

        // logBlocksAllocation(firm);
        return firm;
    }

    private PedalSeries detectPedalSeries(byte[] allBytes) {
        for (PedalSeries pedalSeries : PedalSeries.values()) {
            int pos = ByteUtils.indexOf(allBytes, pedalSeries.getValue().getBytes(), 0);
            if (pos > 0) {
                return pedalSeries;
            }
        }
        return null;
    }

    /**
     * Returns used blocks count.
     *
     * @param firm firmware
     * @return used blocks count
     */
    public int getUsedBlocksCount(Firmware firm) {
        int count = 0;
        for (String block : firm.getBlocks()) {
            if (block != null) {
                count++;
            }
        }
        return count - 1; // without "reserved" first block
    }

    /**
     * Returns total blocks count.
     *
     * @param firm firmware
     * @return total blocks count
     */
    public int getTotalBlocksCount(Firmware firm) {
        return firm.getBinBlocksCount() - Firmware.SYS_BLOCKS_COUNT;
    }

    /**
     * Returns a list of effect names.
     *
     * @param firm firmware
     * @return list of effect names
     */
    ArrayList<String> getEffectNames(Firmware firm) {
        if (firm.getEffects() == null) {
            return new ArrayList<>();
        }
        return firm.getEffects().stream().map(Effect::getFileName).collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Performs save of firmware file.
     *
     * @param firm     firmware
     * @param filePath file system path
     * @return true, if successfully saved
     */
    public boolean saveModifiedFirmwareFile(Firmware firm, Path filePath) {
        log.info("Saving modified firmware file: " + filePath.getFileName().toString());
        try {
            updateFlstSeq(firm);
            if ("true".equalsIgnoreCase(ZoomFirmwareEditor.getProperty("enableDefragmentation"))) {
                defragmentFirmware(firm);
            } else {
                FileTableService.getInstance().rebuildAllFileTables(firm); // required after moving effects
            }
            // read unmodified firmware
            byte[] allBytes = Files.readAllBytes(firm.getFirmwareFile().toPath());
            // replace bin bytes
            System.arraycopy(firm.getDataBytes(), 0, allBytes,
                    firm.getBinStartPosition() + Firmware.BLOCK_SIZE * Firmware.FIRST_DATA_BLOCK, firm.getDataBytes().length);
            // replace system bytes
            System.arraycopy(firm.getSystemBytes(), 0, allBytes, firm.getBinStartPosition(), firm.getSystemBytes().length);
            // save
            Files.write(filePath, allBytes);
            log.info("Success!");
            return true;
        } catch (IOException | ArrayIndexOutOfBoundsException e) {
            log.log(Level.SEVERE, "Failure! " + e.getMessage(), e);
        }
        return false;
    }

    /**
     * Injects a effect into the current firmware.
     *
     * @param firm             firmware
     * @param effect            effect to inject
     * @param rebuildFileTable run file table rebuilding or not
     */
    public void injectEffect(Firmware firm, Effect effect, boolean rebuildFileTable) {
        int blocksCount = EffectService.calculateEffectBlocksCount(effect.getSize());
        log.info("Injecting file: " + effect.getFileName()
                + " (" + blocksCount + " blocks) into " + firm.getFirmwareFile().getName());

        if (getUsedBlocksCount(firm) + blocksCount > getTotalBlocksCount(firm)) {
            log.severe("Effect injection error: not enough free blocks.");
            throw new RuntimeException(ZoomFirmwareEditor.getMessage("notEnoughFreeBlocksError"));
        }

        if (getEffectNames(firm).contains(effect.getFileName())) {
            log.severe("Effect injection error: selected effect is already present in the firmware.");
            throw new RuntimeException(ZoomFirmwareEditor.getMessage("effectIsAlreadyPresentError"));
        }

        // convert bass drives and amps for multistomp pedals (MS-50G, CDR-70)
        if (PedalSeries.ZOOM_MS.equals(firm.getPedalSeries())) {
            if (effect.getType() == (byte) 0x14) {
                effect.setType((byte) 0x0C);
                effect.getContent()[FlstSeqZDT.TYPE_BYTE_POS_IN_ZDL] = effect.getType();
            } else if (effect.getType() == (byte) 0x16) {
                effect.setType((byte) 0x0D);
                effect.getContent()[FlstSeqZDT.TYPE_BYTE_POS_IN_ZDL] = effect.getType();
            }
        }

        // get free block addresses and put address of the first block into the effect's file table item
        int[] reservedBlocks = new int[blocksCount];
        for (int i = 0; i < blocksCount; i++) {
            int address = getFreeBlockAddress(firm);
            if (address == -1) {
                log.severe("Effect injection error: free block is not found.");
                throw new RuntimeException(ZoomFirmwareEditor.getMessage("freeBlockIsNotFoundError")); // that should not happen
            }
            if (i == 0) {
                System.arraycopy(ByteUtils.shortToBytes(Integer.valueOf(address).shortValue()), 0,
                        effect.getFileTableItem(), Effect.ADDR_OFFSET, Effect.ADDR_SIZE);
            }
            reservedBlocks[i] = address;
            firm.getBlocks()[address] = effect.getFileName();
        }

        // insert effect content data into reserved blocks
        for (int i = 0; i < blocksCount; i++) {
            int size;
            byte[] blockInfoBytes = ArrayUtils.makeAndFillArray(Firmware.BLOCK_INFO_SIZE, (byte) 0xFF);
            if (i > 0) {
                // set previous address for all blocks except first
                byte[] previousAddressBytes = ByteUtils.shortToBytes(Integer.valueOf(reservedBlocks[i - 1]).shortValue());
                System.arraycopy(previousAddressBytes, 0, blockInfoBytes, Firmware.BLOCK_PREV_ADDR_OFFSET, Firmware.BLOCK_PREV_ADDR_SIZE);
            }

            if (i < blocksCount - 1) {
                // set next address and maximum data size for all blocks except last
                byte[] nextAddressBytes = ByteUtils.shortToBytes(Integer.valueOf(reservedBlocks[i + 1]).shortValue());
                System.arraycopy(nextAddressBytes, 0, blockInfoBytes, Firmware.BLOCK_NEXT_ADDR_OFFSET, Firmware.BLOCK_NEXT_ADDR_SIZE);
                size = Firmware.BLOCK_SIZE - Firmware.BLOCK_INFO_SIZE;
                byte[] sizeBytes = ByteUtils.shortToBytes(Integer.valueOf(size).shortValue());
                System.arraycopy(sizeBytes, 0, blockInfoBytes, Firmware.BLOCK_SIZE_OFFSET, Firmware.BLOCK_SIZE_SIZE);
            } else {
                // set size for the last block
                size = effect.getSize() % (Firmware.BLOCK_SIZE - Firmware.BLOCK_INFO_SIZE);
                byte[] sizeBytes = ByteUtils.shortToBytes(Integer.valueOf(size).shortValue());
                System.arraycopy(sizeBytes, 0, blockInfoBytes, Firmware.BLOCK_SIZE_OFFSET, Firmware.BLOCK_SIZE_SIZE);
            }

            // inject block into allBytes[]
            int blockStartPos = Firmware.BLOCK_SIZE * reservedBlocks[i];
            System.arraycopy(blockInfoBytes, 0, firm.getDataBytes(), blockStartPos, Firmware.BLOCK_INFO_SIZE);
            System.arraycopy(effect.getContent(), i * (Firmware.BLOCK_SIZE - Firmware.BLOCK_INFO_SIZE),
                    firm.getDataBytes(), blockStartPos + Firmware.BLOCK_INFO_SIZE, size);
            if (size < Firmware.BLOCK_SIZE - Firmware.BLOCK_INFO_SIZE) {
                // put "FF" until the end of last block
                System.arraycopy(ArrayUtils.makeAndFillArray(Firmware.BLOCK_SIZE - Firmware.BLOCK_INFO_SIZE - size, (byte) 0xFF), 0, firm.getDataBytes(),
                        blockStartPos + Firmware.BLOCK_INFO_SIZE + size, Firmware.BLOCK_SIZE - Firmware.BLOCK_INFO_SIZE - size);
            }
        }

        firm.getEffects().add(effect);

        if (firm.getEffects().size() * FileTable.ITEM_SIZE >= Firmware.BLOCK_SIZE * 2) {
            log.severe("Too many effect files! File table will not fit into 2 blocks!");
            throw new RuntimeException(ZoomFirmwareEditor.getMessage("tooManyFilesError"));
        }

        updateFlstSeq(firm);

        if (rebuildFileTable) {
            FileTableService.getInstance().rebuildAllFileTables(firm);
        }
    }

    /**
     * Removes selected effects from the firmware.
     */
    public void removeEffectFile(Firmware firm, ArrayList<String> filesToRemove) {
        try {
            firm.getEffects().removeIf(effect -> filesToRemove.contains(effect.getFileName()));
            for (String fileName : filesToRemove) {
                FileTableService.getInstance().clearBlocksFromFilename(firm, fileName);
            }
            updateFlstSeq(firm);
            FileTableService.getInstance().rebuildAllFileTables(firm);
        } catch (Exception e) {
            log.severe("Effect remove error");
        }
    }

    /**
     * Changes the file order in the firmware: moves selected effect up or down.
     *
     * @param isUp if true, then direction is "up"
     */
    public void moveEffectUpOrDown(Firmware firm, String fileName, boolean isUp) {
        for (int i = 0; i < firm.getEffects().size(); i++) {
            if (fileName.equals(firm.getEffects().get(i).getFileName())) {
                if (isUp && i > 0) {
                    Collections.swap(firm.getEffects(), i, i - 1);
                    break;
                } else if (!isUp && i < firm.getEffects().size() - 1) {
                    Collections.swap(firm.getEffects(), i, i + 1);
                    break;
                }
            }
        }
    }

    /**
     * Finds the address of the first free block.
     *
     * @param firm firmware
     * @return free block number or -1 if free block is not found
     */
    private int getFreeBlockAddress(Firmware firm) {
        String[] blocks = firm.getBlocks();
        for (int i = 1; i < blocks.length; i++) { // NB! start from 1, blocks[0] is reserved
            if (blocks[i] == null) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Performs defragmentation/reorganization of firmware's BIN:
     * all effect content blocks are moved into beginning of firmware's data section and remaining BIN space is filled with "FF" bytes.
     *
     * @param firm firmware
     */
    private void defragmentFirmware(Firmware firm) {
        log.info("Firmware defragmentation...");
        byte[] emptyDataBytes = ArrayUtils.makeAndFillArray(firm.getDataBytes().length, (byte) 0xFF);
        firm.setDataBytes(emptyDataBytes);

        for (int i = 1; i < firm.getBlocks().length; i++) { // NB! start from 1, blocks[0] is reserved
            firm.getBlocks()[i] = null;
        }

        List<Effect> oldEffectList = new ArrayList<>(firm.getEffects());
        firm.setEffects(new ArrayList<>());

        for (Effect effect : oldEffectList) {
            injectEffect(firm, effect, false);
        }

        FileTableService.getInstance().rebuildAllFileTables(firm);
    }

    /**
     * Updates sequence file.
     *
     * @param firm firmware
     */
    private void updateFlstSeq(Firmware firm) {
        if (!"true".equalsIgnoreCase(ZoomFirmwareEditor.getProperty("excludeSequenceFiles"))) {
            for (Effect file : firm.getEffects()) {
                if (FlstSeqZDT.FILE_NAME.equals(file.getFileName())) {
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    file.setContent(ArrayUtils.makeAndFillArray(FlstSeqZDT.FILE_SIZE, (byte) 0x00));

                    try {
                        // TODO: optimize algorithm (sort effects by type?)
                        for (byte type : FlstSeqZDT.DEFAULT_TYPE_ORDER) {
                            ByteArrayOutputStream middleLines = new ByteArrayOutputStream();

                            if (type == (byte) 0x00) {
                                middleLines.write(FlstSeqZDT.EMPTY_LINE.getBytes());
                            } else {
                                for (Effect effect : firm.getEffects()) {
                                    if (type == effect.getType() && effect.getFileName() != null && !Firmware.EXCLUDE_FILENAMES.contains(effect.getFileName())) {
                                        byte[] line = FlstSeqZDT.EMPTY_LINE.getBytes();
                                        System.arraycopy(effect.getFileName().getBytes(), 0, line, 0, effect.getFileName().length());
                                        log.info("Type: " + type + ", effect: " + effect.getFileName());
                                        middleLines.write(line);
                                    }
                                }
                            }

                            if (middleLines.size() > 0) {
                                byte[] line = FlstSeqZDT.OPENING_LINE;
                                line[FlstSeqZDT.TYPE_BYTE_POS_IN_SEQ] = type;
                                outputStream.write(line);

                                outputStream.write(middleLines.toByteArray());

                                line = FlstSeqZDT.ENDING_LINE;
                                line[FlstSeqZDT.TYPE_BYTE_POS_IN_SEQ] = type;
                                outputStream.write(line);
                            }
                        }
                    } catch (IOException e) {
                        log.severe(e.getMessage());
                    }

                    byte[] output = outputStream.toByteArray();
                    System.arraycopy(output, 0, file.getContent(), 0, output.length);
                    break;
                }
            }
        }
    }

    /**
     * Prints block allocation table.
     *
     * @param firm firmware
     */
    private void logBlocksAllocation(Firmware firm) {
        for (int i = 0; i < firm.getBlocks().length; i++) {
            if (firm.getBlocks()[i] != null) {
                log.info("Block: " + i + " contains " + firm.getBlocks()[i]);
            } else {
                log.info("Block: " + i + " is not used!!");
            }
        }
    }

}

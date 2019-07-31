package main.java.zoomeditor.service;

import main.java.ZoomFirmwareEditor;
import main.java.zoomeditor.model.FileTable;
import main.java.zoomeditor.model.Firmware;
import main.java.zoomeditor.model.FlstSeqZDT;
import main.java.zoomeditor.model.Patch;
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
        FileTableService.getInstance().fillPatchesAndBlocks(firm);

        // logBlocksAllocation(firm);
        return firm;
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
     * Returns a list of patch names.
     *
     * @param firm firmware
     * @return list of natch names
     */
    ArrayList<String> getPatchNames(Firmware firm) {
        if (firm.getPatches() == null) {
            return new ArrayList<>();
        }
        return firm.getPatches().stream().map(Patch::getFileName).collect(Collectors.toCollection(ArrayList::new));
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
            if ("true".equalsIgnoreCase(ZoomFirmwareEditor.getProperty("enableDefragmentation"))) {
                defragmentFirmware(firm);
            } else {
                FileTableService.getInstance().rebuildAllFileTables(firm); // required after moving patches
            }
            if (!"true".equalsIgnoreCase(ZoomFirmwareEditor.getProperty("excludeSequenceFiles"))) {
                updateFlstSeq(firm);
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
     * Injects a patch into the current firmware.
     *
     * @param firm             firmware
     * @param patch            patch to inject
     * @param rebuildFileTable run file table rebuilding or not
     */
    public void injectPatch(Firmware firm, Patch patch, boolean rebuildFileTable) {
        int blocksCount = PatchService.calculatePatchBlocksCount(patch.getSize());
        log.info("Injecting file: " + patch.getFileName()
                + " (" + blocksCount + " blocks) into " + firm.getFirmwareFile().getName());

        if (getUsedBlocksCount(firm) + blocksCount > getTotalBlocksCount(firm)) {
            log.severe("Patch injection error: not enough free blocks.");
            throw new RuntimeException(ZoomFirmwareEditor.getMessage("notEnoughFreeBlocksError"));
        }

        if (getPatchNames(firm).contains(patch.getFileName())) {
            log.severe("Patch injection error: selected patch is already present in the firmware.");
            throw new RuntimeException(ZoomFirmwareEditor.getMessage("patchIsAlreadyPresentError"));
        }

        // get free block addresses and put address of the first block into the patch's file table item
        int[] reservedBlocks = new int[blocksCount];
        for (int i = 0; i < blocksCount; i++) {
            int address = getFreeBlockAddress(firm);
            if (address == -1) {
                log.severe("Patch injection error: free block is not found.");
                throw new RuntimeException(ZoomFirmwareEditor.getMessage("freeBlockIsNotFoundError")); // that should not happen
            }
            if (i == 0) {
                System.arraycopy(ByteUtils.shortToBytes(Integer.valueOf(address).shortValue()), 0,
                        patch.getFileTableItem(), Patch.ADDR_OFFSET, Patch.ADDR_SIZE);
            }
            reservedBlocks[i] = address;
            firm.getBlocks()[address] = patch.getFileName();
        }

        // insert patch content data into reserved blocks
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
                size = patch.getSize() % (Firmware.BLOCK_SIZE - Firmware.BLOCK_INFO_SIZE);
                byte[] sizeBytes = ByteUtils.shortToBytes(Integer.valueOf(size).shortValue());
                System.arraycopy(sizeBytes, 0, blockInfoBytes, Firmware.BLOCK_SIZE_OFFSET, Firmware.BLOCK_SIZE_SIZE);
            }

            // inject block into allBytes[]
            int blockStartPos = Firmware.BLOCK_SIZE * reservedBlocks[i];
            System.arraycopy(blockInfoBytes, 0, firm.getDataBytes(), blockStartPos, Firmware.BLOCK_INFO_SIZE);
            System.arraycopy(patch.getContent(), i * (Firmware.BLOCK_SIZE - Firmware.BLOCK_INFO_SIZE),
                    firm.getDataBytes(), blockStartPos + Firmware.BLOCK_INFO_SIZE, size);
            if (size < Firmware.BLOCK_SIZE - Firmware.BLOCK_INFO_SIZE) {
                // put "FF" until the end of last block
                System.arraycopy(ArrayUtils.makeAndFillArray(Firmware.BLOCK_SIZE - Firmware.BLOCK_INFO_SIZE - size, (byte) 0xFF), 0, firm.getDataBytes(),
                        blockStartPos + Firmware.BLOCK_INFO_SIZE + size, Firmware.BLOCK_SIZE - Firmware.BLOCK_INFO_SIZE - size);
            }
        }

        firm.getPatches().add(patch);

        if (firm.getPatches().size() * FileTable.ITEM_SIZE >= Firmware.BLOCK_SIZE * 2) {
            log.severe("Too many patch files! File table will not fit into 2 blocks!");
            throw new RuntimeException(ZoomFirmwareEditor.getMessage("tooManyFilesError"));
        }

        if (rebuildFileTable) {
            FileTableService.getInstance().rebuildAllFileTables(firm);
        }
    }

    /**
     * Removes selected patches from the firmware.
     */
    public void removePatchFile(Firmware firm, ArrayList<String> filesToRemove) {
        try {
            firm.getPatches().removeIf(patch -> filesToRemove.contains(patch.getFileName()));
            for (String fileName : filesToRemove) {
                FileTableService.getInstance().clearBlocksFromFilename(firm, fileName);
            }
            FileTableService.getInstance().rebuildAllFileTables(firm);
        } catch (Exception e) {
            log.severe("Patch remove error");
        }
    }

    /**
     * Changes the file order in the firmware: moves selected patch up or down.
     *
     * @param isUp if true, then direction is "up"
     */
    public void movePatchUpOrDown(Firmware firm, String fileName, boolean isUp) {
        for (int i = 0; i < firm.getPatches().size(); i++) {
            if (fileName.equals(firm.getPatches().get(i).getFileName())) {
                if (isUp && i > 0) {
                    Collections.swap(firm.getPatches(), i, i - 1);
                    break;
                } else if (!isUp && i < firm.getPatches().size() - 1) {
                    Collections.swap(firm.getPatches(), i, i + 1);
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
     * all patch content blocks are moved into beginning of firmware's data section and remaining BIN space is filled with "FF" bytes.
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

        List<Patch> oldPatchList = new ArrayList<>(firm.getPatches());
        firm.setPatches(new ArrayList<>());

        for (Patch patch : oldPatchList) {
            injectPatch(firm, patch, false);
        }

        FileTableService.getInstance().rebuildAllFileTables(firm);
    }

    /**
     * Updates sequence file.
     *
     * @param firm firmware
     */
    private void updateFlstSeq(Firmware firm) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            for (byte type : FlstSeqZDT.TYPE_ORDER) {
                byte[] line = FlstSeqZDT.OPENING_LINE;
                line[FlstSeqZDT.TYPE_BYTE_POS_IN_SEQ] = type;
                // TODO: do not write types without effects (except type 0)
                outputStream.write(line);

                if (type == (byte) 0x00) {
                    outputStream.write(FlstSeqZDT.EMPTY_LINE);
                } else {
                    // TODO: optimize algorithm (sort effects by type?)
                    for (Patch patch : firm.getPatches()) {
                        if (type == patch.getType() && patch.getFileName() != null && !Firmware.EXCLUDE_FILENAMES.contains(patch.getFileName())) {
                            line = FlstSeqZDT.EMPTY_LINE;
                            // TODO: Fix "CAVE.ZDLLLDL." etc
                            System.arraycopy(patch.getFileName().getBytes(), 0, line, 0, patch.getFileName().length());
                            log.info("Type: " + type + ", effect: " + patch.getFileName());
                            outputStream.write(line);
                        }
                    }
                }

                line = FlstSeqZDT.ENDING_LINE;
                line[FlstSeqZDT.TYPE_BYTE_POS_IN_SEQ] = type;
                outputStream.write(line);
            }
        } catch (IOException e) {
            log.severe(e.getMessage());
        }

        for (Patch patch : firm.getPatches()) {
            if ("FLST_SEQ.ZDT".equals(patch.getFileName())) {
                patch.setContent(outputStream.toByteArray());
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

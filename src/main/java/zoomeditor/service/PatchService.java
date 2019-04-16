package main.java.zoomeditor.service;

import main.java.ZoomFirmwareEditor;
import main.java.zoomeditor.model.FileTable;
import main.java.zoomeditor.model.Firmware;
import main.java.zoomeditor.model.Patch;
import main.java.zoomeditor.util.ArrayUtils;
import main.java.zoomeditor.util.ByteUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PatchService {
    private static volatile PatchService instance = null;
    private static final Logger log = Logger.getLogger(PatchService.class.getName());

    private PatchService() {
    }

    public static PatchService getInstance() {
        if (instance == null) {
            synchronized (PatchService.class) {
                if (instance == null) {
                    instance = new PatchService();
                }
            }
        }
        return instance;
    }

    /**
     * Makes patch object from file table item.
     *
     * @param fileTableItem file table item
     * @return patch
     */
    static Patch makePatchFromFileTableItem(byte[] fileTableItem) {
        final byte[] empty = ArrayUtils.makeAndFillArray(FileTable.SYSTEM_DATA_SIZE, (byte) 0xFF);
        if (fileTableItem == null || fileTableItem.length == 0
                || Arrays.equals(ArrayUtils.copyPart(fileTableItem, 0, empty.length), empty)) {
            return null;
        }
        Patch patch = new Patch();
        try {
            patch.setFileTableItem(fileTableItem);
            patch.setFileName(patch.extractFileNameFromFileTableItem());
            patch.setAddress(ByteUtils.bytesToUnsignedShortAsInt(patch.getAddressBytes()));
            patch.setSize(ByteUtils.bytesToInt(patch.getSizeBytes()));
        } catch (NumberFormatException e) {
            log.severe(e.getMessage() + "\nFile table item:\n" + ByteUtils.bytesToHexString(fileTableItem));
            return null;
        }
        return patch;
    }

    /**
     * Makes patch object from file.
     *
     * @param patchFile patch file
     * @return patch
     */
    public Patch makePatchFromFile(File patchFile) {
        log.info("patchFile: " + patchFile.getAbsolutePath());
        if (patchFile.getName().length() > Patch.FILENAME_SIZE) {
            log.severe("File name is too long: " + patchFile.getName());
            throw new RuntimeException(ZoomFirmwareEditor.getMessage("tooLongFileNameErrorBeginning") + " \""
                    + patchFile.getName() + "\" " + ZoomFirmwareEditor.getMessage("tooLongFileNameErrorEnding"));
        }
        try {
            byte[] patchContent = Files.readAllBytes(patchFile.toPath());
            byte[] sizeBytes = ByteUtils.intToBytes(patchContent.length);
            byte[] fileTableItem = ArrayUtils.makeAndFillArray(FileTable.ITEM_SIZE, (byte) 0xFF);
            fileTableItem[Patch.ADDR_OFFSET + Patch.ADDR_SIZE] = (byte) 0x01;
            System.arraycopy(sizeBytes, 0, fileTableItem, Patch.SIZE_OFFSET, Patch.SIZE_SIZE);
            System.arraycopy(ArrayUtils.makeAndFillArray(FileTable.ITEM_SIZE, (byte) 0x00), 0,
                    fileTableItem, Patch.FILENAME_OFFSET, Patch.FILENAME_SIZE);
            System.arraycopy(patchFile.getName().getBytes(), 0, fileTableItem, Patch.FILENAME_OFFSET,
                    patchFile.getName().length());
            // NB! Address is not set! It should be assigned during injectPatch().

            Patch patch = makePatchFromFileTableItem(fileTableItem);
            if (patch == null) {
                log.severe("Patch is null!");
                throw new RuntimeException(ZoomFirmwareEditor.getMessage("patchLoadingError"));
            }

            patch.setContent(patchContent);
            patch.setName(patch.extractNameFromContent());

            // log.info(patch.toString());
            return patch;
        } catch (Exception e) {
            log.log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(ZoomFirmwareEditor.getMessage("patchLoadingError"));
        }
    }

    /**
     * Calculates the patch blocks count.
     *
     * @param size patch size in bytes
     * @return blocks count
     */
    public static int calculatePatchBlocksCount(int size) {
        return (int) Math.ceil((double) size / (Firmware.BLOCK_SIZE - Firmware.BLOCK_INFO_SIZE));
    }

    /**
     * Performs save of patch file.
     *
     * @param firm             firmware
     * @param originalFileName file name in the patches list and files table
     * @param filePath         file system path
     * @return true if saved successfully
     */
    public boolean savePatchFile(Firmware firm, String originalFileName, Path filePath) {
        log.info("Saving file: " + originalFileName + " as " + filePath.getFileName().toString());
        for (Patch patch : firm.getPatches()) {
            if (originalFileName.equals(patch.getFileName())) {
                try {
                    Files.write(filePath, patch.getContent());
                    log.info("Success!");
                    return true;
                } catch (IOException e) {
                    log.log(Level.SEVERE, "Failure! " + e.getMessage(), e);
                    return false;
                }
            }
        }
        return false;
    }

}

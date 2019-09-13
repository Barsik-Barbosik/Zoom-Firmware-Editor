package main.java.zoomeditor.service;

import main.java.ZoomFirmwareEditor;
import main.java.zoomeditor.model.Effect;
import main.java.zoomeditor.model.FileTable;
import main.java.zoomeditor.model.Firmware;
import main.java.zoomeditor.util.ArrayUtils;
import main.java.zoomeditor.util.ByteUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EffectService {
    private static volatile EffectService instance = null;
    private static final Logger log = Logger.getLogger(EffectService.class.getName());

    private EffectService() {
    }

    public static EffectService getInstance() {
        if (instance == null) {
            synchronized (EffectService.class) {
                if (instance == null) {
                    instance = new EffectService();
                }
            }
        }
        return instance;
    }

    /**
     * Makes effect object from file table item.
     *
     * @param fileTableItem file table item
     * @return effect
     */
    static Effect makeEffectFromFileTableItem(byte[] fileTableItem) {
        final byte[] empty = ArrayUtils.makeAndFillArray(FileTable.SYSTEM_DATA_SIZE, (byte) 0xFF);
        if (fileTableItem == null || fileTableItem.length == 0
                || Arrays.equals(ArrayUtils.copyPart(fileTableItem, 0, empty.length), empty)) {
            return null;
        }
        Effect effect = new Effect();
        try {
            effect.setFileTableItem(fileTableItem);
            effect.setFileName(effect.extractFileNameFromFileTableItem());
            effect.setAddress(ByteUtils.bytesToUnsignedShortAsInt(effect.getAddressBytes()));
            effect.setSize(ByteUtils.bytesToInt(effect.getSizeBytes()));
        } catch (NumberFormatException e) {
            log.severe(e.getMessage() + "\nFile table item:\n" + ByteUtils.bytesToHexString(fileTableItem));
            return null;
        }
        return effect;
    }

    /**
     * Makes effect object from file.
     *
     * @param effectFile effect file
     * @return effect
     */
    public Effect makeEffectFromFile(File effectFile) {
        log.info("effectFile: " + effectFile.getAbsolutePath());
        if (effectFile.getName().length() > Effect.FILENAME_SIZE) {
            log.severe("File name is too long: " + effectFile.getName());
            throw new RuntimeException(ZoomFirmwareEditor.getMessage("tooLongFileNameErrorBeginning") + " \""
                    + effectFile.getName() + "\" " + ZoomFirmwareEditor.getMessage("tooLongFileNameErrorEnding"));
        }
        try {
            byte[] effectContent = Files.readAllBytes(effectFile.toPath());
            byte[] sizeBytes = ByteUtils.intToBytes(effectContent.length);
            byte[] fileTableItem = ArrayUtils.makeAndFillArray(FileTable.ITEM_SIZE, (byte) 0xFF);
            fileTableItem[Effect.ADDR_OFFSET + Effect.ADDR_SIZE] = (byte) 0x01;
            System.arraycopy(sizeBytes, 0, fileTableItem, Effect.SIZE_OFFSET, Effect.SIZE_SIZE);
            System.arraycopy(ArrayUtils.makeAndFillArray(FileTable.ITEM_SIZE, (byte) 0x00), 0,
                    fileTableItem, Effect.FILENAME_OFFSET, Effect.FILENAME_SIZE);
            System.arraycopy(effectFile.getName().getBytes(), 0, fileTableItem, Effect.FILENAME_OFFSET,
                    effectFile.getName().length());
            // NB! Address is not set! It should be assigned during injectEffect().

            Effect effect = makeEffectFromFileTableItem(fileTableItem);
            if (effect == null) {
                log.severe("Effect is null!");
                throw new RuntimeException(ZoomFirmwareEditor.getMessage("effectLoadingError"));
            }

            effect.setContent(effectContent);
            effect.setName(effect.extractNameFromContent());
            effect.setType(effect.extractTypeFromContent());

            // log.info(effect.toString());
            return effect;
        } catch (Exception e) {
            log.log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(ZoomFirmwareEditor.getMessage("effectLoadingError"));
        }
    }


    public static String getEffectTypeName(byte type, String filename) {
        if (filename.toUpperCase().endsWith("ZDL")) {
            // TODO: enum
            switch(type) {
                case (byte) 0x01:
                    return "DYNAMICS";
                case (byte) 0x02:
                    return "FILTER";
                case (byte) 0x03:
                    return "GUITAR DRIVE";
                case (byte) 0x0C:
                    return "BASS DRIVE"; // MS60B
                case (byte) 0x0D:
                    return "BASS DRIVE"; // MS60B
                case (byte) 0x14:
                    return "BASS DRIVE";
                case (byte) 0x16:
                    return "BASS DRIVE";
                case (byte) 0x04:
                    return "GUTAR AMP";
                case (byte) 0x05:
                    return "BASS AMP";
                case (byte) 0x06:
                    return "MODULATION";
                case (byte) 0x07:
                    return "SFX";
                case (byte) 0x08:
                    return "DELAY";
                case (byte) 0x09:
                    return "REVERB";
                case (byte) 0x0B:
                    return "PEDAL";
                default:
                    return String.format("TYPE \"%02x\"", type);
            }
        } else if (filename.toUpperCase().endsWith("RAW")) {
            return "DRUM SOUND";
        } else {
            return "";
        }
    }

    /**
     * Calculates the effect blocks count.
     *
     * @param size effect size in bytes
     * @return blocks count
     */
    public static int calculateEffectBlocksCount(int size) {
        return (int) Math.ceil((double) size / (Firmware.BLOCK_SIZE - Firmware.BLOCK_INFO_SIZE));
    }

    /**
     * Performs save of effect file.
     *
     * @param firm             firmware
     * @param originalFileName file name in the effects list and files table
     * @param filePath         file system path
     * @return true if saved successfully
     */
    public boolean saveEffectFile(Firmware firm, String originalFileName, Path filePath) {
        log.info("Saving file: " + originalFileName + " as " + filePath.getFileName().toString());
        for (Effect effect : firm.getEffects()) {
            if (originalFileName.equals(effect.getFileName())) {
                try {
                    Files.write(filePath, effect.getContent());
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

package main.java.zoomeditor.service;

import main.java.zoomeditor.model.FileTable;
import main.java.zoomeditor.model.Firmware;
import main.java.zoomeditor.model.Patch;
import main.java.zoomeditor.model.SafetyReport;

import java.util.Locale;

/**
 * Pre-mutation safety checks. Brick prevention lives here.
 * All methods are side-effect free: they never mutate Firmware.
 */
public class SafetyService {
    private static volatile SafetyService instance = null;

    /** File that must never be removed from Four-series (ZD2) firmware. */
    public static final String GUARD_FILE = "GUARDZDL.ZT2";
    /** Shared driver required when a guitar amp goes into a bass unit. */
    public static final String CMN_DRV = "CMN_DRV.ZDL";

    private SafetyService() {
    }

    public static SafetyService getInstance() {
        if (instance == null) {
            synchronized (SafetyService.class) {
                if (instance == null) {
                    instance = new SafetyService();
                }
            }
        }
        return instance;
    }

    /**
     * ZDL family (G1on/B1on/MS-50G/60B/70CDR) vs ZD2 family (G3n/G5n/Four).
     * Verified against real updaters: G1on v1.21 and B1on v1.30 report 0x01,
     * MS-70CDR v2.10 reports 0x02, MS-60B v2.10 is mixed 0x01/0x02 (tables
     * written by different updater versions). All are ZDL-era. Only 0x05
     * means ZD2. Scans every table slot instead of trusting the first.
     */
    public String detectFirmwareFamily(Firmware firm) {
        if (firm == null || firm.getSystemBytes() == null) {
            return "UNKNOWN";
        }
        boolean zd2 = false;
        boolean zdl = false;
        for (int i = 0; i < 4; i++) {
            int position = Firmware.BLOCK_SIZE * (3 + i * 2);
            if (position + 7 >= firm.getSystemBytes().length) {
                continue;
            }
            // valid table marker: 2nd byte A5 (same rule as FileTableService)
            if (firm.getSystemBytes()[position + 1] != (byte) 0xA5) {
                continue;
            }
            byte b = firm.getSystemBytes()[position + 3];
            if (b == (byte) 0x01 || b == (byte) 0x02) {
                zdl = true;
            } else if (b == (byte) 0x05) {
                zd2 = true;
            }
        }
        if (zdl) {
            return "ZDL";
        }
        if (zd2) {
            return "ZD2";
        }
        return "UNKNOWN";
    }

    /** Patch family by file extension. RAW samples are neutral (fit both). */
    public String detectPatchFamily(String fileName) {
        if (fileName == null) {
            return "UNKNOWN";
        }
        String n = fileName.toUpperCase(Locale.ROOT);
        if (n.endsWith(".ZDL")) {
            return "ZDL";
        }
        if (n.endsWith(".ZD2") || n.endsWith(".ZT2") || n.endsWith(".ZDT")) {
            return "ZD2";
        }
        if (n.endsWith(".RAW")) {
            return "NEUTRAL";
        }
        return "UNKNOWN";
    }

    public boolean isBassFirmware(Firmware firm) {
        if (firm == null || firm.getFirmwareFile() == null) {
            return false;
        }
        String n = firm.getFirmwareFile().getName().toUpperCase(Locale.ROOT);
        return n.contains("B1") || n.contains("B3") || n.contains("60B");
    }

    /** Heuristic: does this patch look like a guitar amp/drive/cab model? */
    public boolean looksLikeGuitarAmp(String fileName) {
        if (fileName == null) {
            return false;
        }
        String n = fileName.toUpperCase(Locale.ROOT);
        return n.contains("AMP") || n.contains("DRV") || n.contains("CAB")
                || n.equals(CMN_DRV);
    }

    /** Full dry-run before inject. Never mutates firmware. */
    public SafetyReport validateInject(Firmware firm, Patch patch) {
        SafetyReport r = new SafetyReport();
        if (firm == null || patch == null) {
            r.addError("No firmware or patch loaded.");
            return r;
        }
        String fwFam = detectFirmwareFamily(firm);
        String pFam = detectPatchFamily(patch.getFileName());
        r.setFirmwareFamily(fwFam);
        r.setPatchFamily(pFam);

        // 1. Family mismatch is a hard block (ZDL pedals cannot run ZD2 fx and vice versa).
        if (!"NEUTRAL".equals(pFam) && !"UNKNOWN".equals(fwFam)
                && !"UNKNOWN".equals(pFam) && !fwFam.equals(pFam)) {
            r.addError("Family mismatch: " + patch.getFileName() + " is " + pFam
                    + " but firmware is " + fwFam + ". Cross-flashing bricks or silently fails.");
        }

        FirmwareService fs = FirmwareService.getInstance();
        int need = PatchService.calculatePatchBlocksCount(patch.getSize());
        int used = 0;
        int total = 0;
        try {
            used = fs.getUsedBlocksCount(firm);
            total = fs.getTotalBlocksCount(firm);
        } catch (Exception e) {
            r.addError("Block table unreadable: " + e.getMessage());
            return r;
        }
        // 2. Space check BEFORE reserving anything.
        if (used + need > total) {
            r.addError("Not enough free blocks: need " + need
                    + ", free " + (total - used) + " of " + total + ".");
        }
        // 3. File-table overflow check BEFORE mutating (2 blocks max).
        if ((firm.getPatches().size() + 1) * FileTable.ITEM_SIZE >= Firmware.BLOCK_SIZE * 2) {
            r.addError("File table would overflow 2 blocks. Remove an effect first.");
        }
        // 4. Duplicate.
        if (fs.getPatchNames(firm).contains(patch.getFileName())) {
            r.addError("Patch " + patch.getFileName() + " is already in this firmware.");
        }
        // 5. Guitar amp into bass unit without shared driver.
        if (isBassFirmware(firm) && looksLikeGuitarAmp(patch.getFileName())
                && !patch.getFileName().equalsIgnoreCase(CMN_DRV)
                && !fs.getPatchNames(firm).contains(CMN_DRV)) {
            r.addError("Bass firmware needs " + CMN_DRV
                    + " alongside guitar amp " + patch.getFileName() + ". Inject " + CMN_DRV + " first.");
        }
        // 6. Exact-multiple size edge case (last block would encode 0).
        int payload = Firmware.BLOCK_SIZE - Firmware.BLOCK_INFO_SIZE;
        if (patch.getSize() > 0 && patch.getSize() % payload == 0) {
            r.addWarning("Size " + patch.getSize() + " is an exact multiple of " + payload
                    + "; last-block header is re-encoded as full on save.");
        }
        return r;
    }

    /** Dry-run before removal. GUARD file removal is a hard block. */
    public SafetyReport validateRemove(Firmware firm, java.util.List<String> filesToRemove) {
        SafetyReport r = new SafetyReport();
        r.setFirmwareFamily(detectFirmwareFamily(firm));
        if (filesToRemove == null || filesToRemove.isEmpty()) {
            r.addError("Nothing selected.");
            return r;
        }
        for (String f : filesToRemove) {
            if (GUARD_FILE.equalsIgnoreCase(f)) {
                r.addError(GUARD_FILE + " protects Four-series boot. Removing it can brick the pedal. Refused.");
            }
            if (CMN_DRV.equalsIgnoreCase(f)) {
                r.addWarning("Removing " + CMN_DRV + " breaks any guitar amps left in a bass unit.");
            }
        }
        return r;
    }
}

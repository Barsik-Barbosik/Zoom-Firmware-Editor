package zoomeditor.service;

import main.java.zoomeditor.model.Firmware;
import main.java.zoomeditor.model.Patch;
import main.java.zoomeditor.model.SafetyReport;
import main.java.zoomeditor.service.SafetyService;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SafetyServiceTest {

    private Firmware zdlFirmware(String fileName, int totalBlocks) {
        Firmware f = new Firmware(new File(fileName));
        byte[] sys = new byte[Firmware.BLOCK_SIZE * Firmware.SYS_BLOCKS_COUNT];
        Arrays.fill(sys, (byte) 0xFF);
        int pos = Firmware.BLOCK_SIZE * 3; // first table slot
        sys[pos + 1] = (byte) 0xA5;
        sys[pos + 3] = (byte) 0x01; // ZDL generation
        sys[pos + 4] = (byte) 0xFF; // in use
        f.setSystemBytes(sys);
        f.setPatches(new ArrayList<>());
        String[] blocks = new String[totalBlocks];
        blocks[0] = "RESERVED (part of file table)";
        f.setBlocks(blocks);
        f.setBinBlocksCount(totalBlocks + Firmware.SYS_BLOCKS_COUNT);
        return f;
    }

    private Patch patch(String name, int size) {
        Patch p = new Patch();
        p.setFileName(name);
        p.setSize(size);
        p.setContent(new byte[size]);
        return p;
    }

    @Test
    public void patchFamilyByExtension() {
        SafetyService s = SafetyService.getInstance();
        assertEquals("ZDL", s.detectPatchFamily("CHORUS.ZDL"));
        assertEquals("ZD2", s.detectPatchFamily("CHORUS.ZD2"));
        assertEquals("ZD2", s.detectPatchFamily("GUARDZDL.ZT2"));
        assertEquals("NEUTRAL", s.detectPatchFamily("KICK.RAW"));
    }

    @Test
    public void familyMismatchIsBlocked() {
        SafetyService s = SafetyService.getInstance();
        Firmware fw = zdlFirmware("G1on_v1.21_Win_E.exe", 100);
        Patch p = patch("SOME.ZD2", 100);
        SafetyReport r = s.validateInject(fw, p);
        assertTrue(r.isBlocked());
        assertEquals("ZDL", r.getFirmwareFamily());
        assertEquals("ZD2", r.getPatchFamily());
    }

    @Test
    public void sameFamilyPassesWhenSpaceAllows() {
        SafetyService s = SafetyService.getInstance();
        Firmware fw = zdlFirmware("G1on_v1.21_Win_E.exe", 100);
        Patch p = patch("CHORUS.ZDL", 100);
        SafetyReport r = s.validateInject(fw, p);
        assertFalse(r.isBlocked());
    }

    @Test
    public void msSeriesMarker02CountsAsZdl() {
        // Real-world: MS-70CDR v2.10 reports 0x02, MS-60B v2.10 is mixed 0x01/0x02.
        SafetyService s = SafetyService.getInstance();
        Firmware fw = zdlFirmware("MS-70CDR_v2.10.exe", 100);
        fw.getSystemBytes()[Firmware.BLOCK_SIZE * 3 + 3] = (byte) 0x02;
        assertEquals("ZDL", s.detectFirmwareFamily(fw));
        // mixed 0x01/0x02 (as seen in MS-60B v2.10) is still ZDL
        fw.getSystemBytes()[Firmware.BLOCK_SIZE * 5 + 1] = (byte) 0xA5;
        fw.getSystemBytes()[Firmware.BLOCK_SIZE * 5 + 3] = (byte) 0x02;
        assertEquals("ZDL", s.detectFirmwareFamily(fw));
    }

    @Test
    public void guitarAmpIntoBassWithoutDriverIsBlocked() {
        SafetyService s = SafetyService.getInstance();
        Firmware fw = zdlFirmware("B1on_v1.21_Win_E.exe", 100);
        // heuristic keys on AMP/DRV/CAB substrings
        assertTrue(s.looksLikeGuitarAmp("MYAMP.ZDL"));
        assertFalse(s.looksLikeGuitarAmp("CHORUS.ZDL"));
        Patch marked = patch("MYAMP.ZDL", 100);
        SafetyReport r = s.validateInject(fw, marked);
        assertTrue(r.isBlocked(), "bass fw + guitar amp without CMN_DRV must block");
        // once the shared driver is present, the same amp passes the driver rule
        Patch drv = patch("CMN_DRV.ZDL", 100);
        fw.getPatches().add(drv);
        assertFalse(s.validateInject(fw, marked).isBlocked());
    }

    @Test
    public void guardFileRemovalIsBlocked() {
        SafetyService s = SafetyService.getInstance();
        Firmware fw = zdlFirmware("G1XFOUR_v2.0.exe", 100);
        List<String> rm = Arrays.asList("GUARDZDL.ZT2");
        assertTrue(s.validateRemove(fw, rm).isBlocked());
        List<String> ok = Arrays.asList("CHORUS.ZDL");
        assertFalse(s.validateRemove(fw, ok).isBlocked());
    }

    @Test
    public void exactMultipleSizeWarnsButDoesNotBlock() {
        SafetyService s = SafetyService.getInstance();
        Firmware fw = zdlFirmware("G1on_v1.21_Win_E.exe", 100);
        int payload = Firmware.BLOCK_SIZE - Firmware.BLOCK_INFO_SIZE;
        Patch p = patch("CHORUS.ZDL", payload);
        SafetyReport r = s.validateInject(fw, p);
        assertFalse(r.isBlocked());
        assertTrue(r.hasWarnings());
    }
}

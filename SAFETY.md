# SAFETY — read before you flash

This tool edits Zoom **updater EXEs offline** and produces a `MODIFIED *.exe`.
Flashing a bad mod can **brick your pedal** (no boot, no recovery button on
G1on/B1on/MS family). The 0.99-safety fork adds guards, but guards are not a
guarantee. Follow this checklist every time.

## The 7 rules

1. **Keep the original.** On first save the app writes `<source>.orig.bak`
   next to the file you opened (`FirmwareService.backupOriginal()`).
   Never delete it. Verify it exists before flashing anything.
2. **Never unplug / power off mid-flash.** A torn write = brick. Laptop on
   charger, direct USB port, no hub, close other audio/USB apps.
3. **ZDL and ZD2 never mix.** G1on/G1Xon/B1on/MS-50G/60B/70CDR = ZDL.
   G3n/G5n/Four = ZD2. Cross-family inject is now **blocked**
   (`SafetyService.validateInject()`), but double-check the filename
   extensions yourself: `.ZDL` vs `.ZD2/.ZT2/.ZDT`. `.RAW` drum samples are neutral.
4. **Bass units need CMN_DRV.ZDL.** Guitar amp into B1on/B1Xon/MS-60B without
   `CMN_DRV.ZDL` = silent DSP or brick. Now blocked until you inject
   `CMN_DRV.ZDL` first.
5. **Never remove GUARDZDL.ZT2** on Four-series firmware. Removal is now
   refused. If you see it, leave it alone.
6. **Watch the blocks bar.** `used + need > total` and file-table overflow
   (2 blocks max) are pre-checked before any byte is reserved. If it says
   remove something first, do it — do not force it.
7. **Prefer the safe flasher.** For day-to-day synth curation, flash with
   **Zoom Effect Manager ≥2.3** (writes effect data only, interrupt-safe).
   Use this editor for offline curation/analysis and keep its `MODIFIED .exe`
   as a second path, not the first.

## What the app now does automatically

- Dry-run validation before inject/remove (`SafetyService`, side-effect free).
- Family-mismatch, duplicate, out-of-space, table-overflow, CMN_DRV, GUARD
  blocks with popups (`ApplicationController`).
- Warning confirm dialog (exact-multiple sizes, CMN_DRV removal).
- `.orig.bak` snapshot of the original updater on save.
- Last-block header fix for effects whose size is an exact multiple of 4090
  (previously encoded a 0-length last block).

## If something goes wrong

1. Do NOT retry blindly. Close the updater, leave the pedal alone 30s.
2. Re-flash the **original, unmodified** updater from Zoom (`zoom.co.jp`).
3. If the pedal is not recognised, try another USB port/cable, then Zoom
   support. There is no documented DFU/jumper recovery on G1on.

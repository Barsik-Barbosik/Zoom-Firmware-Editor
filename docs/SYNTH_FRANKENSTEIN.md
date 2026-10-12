# SYNTH FRANKENSTEIN — G1on as a synth multi-FX (no metal, no drums)

Volca Beats owns drums. This pedal owns space/mod/filter.

## Validated build (2026-10-12, headless, all safety gates passed)

`MODIFIED Zoom G1on System v1.21 SYNTH Updater.exe` (in `firmwares/`, NOT in
git — Zoom binaries are copyrighted, download your own from zoomcorp.com):

- Base: `G1on_v1.21` (125 patches, 559/778 blocks, family `ZDL`/`0x01`).
- Donor pools: `B1on_v1.30` (124 patches, `0x01`), `MS-70CDR_v2.10`
  (137 patches, `0x02`), `MS-60B_v2.10` (143 patches, mixed `0x01`/`0x02`).
  All ZDL-era — G1on-compatible. (`0x02` is the v2.x table marker; only
  `0x05` means ZD2. See `SafetyService.detectFirmwareFamily()`.)
- Stripped 39 files (204 blocks): 15 amp/cab/metal/guitar-only
  (`ACOSIM, FDCOMBO, FD_VIBRO, BGN_DRV, BG_CRNCH, BG_DRV, B_BRKR, CAR_DRV,
  DELUXE_R, DZ_DRIVE, HW_STACK, EXTRMDS, LEAD, Z_SCREAM, CRY`) + all 24 drum
  `*.RAW` (`Ride.raw` alone was 17 blocks).
- Injected 49 files (276 blocks), every one through `validateInject()`:
  - Chorus/mod: `VTGCE(VintageCE), CLONECHO, STONEPHA, BF_FLG_2, MIRAGECH,
    SILKYCHO, ANA234CH, CRN_CHO, AUTOPAN, DUOTREM`
  - Reverb: `SHIMMER(9blk), CHURCH, CHAMBER, HD_REV, SPC_HOLE(11blk),
    MNGLD_SP(12blk), GATE_REV, DUAL_REV(11blk), AMBIENCE, CAVE, LOFI_REV,
    SPRING, ECHO`
  - Delay: `ANLGDLY, MODDLY, MODDLY2, DUALDIGD(9blk), PHASEDLY, ICEDLY(8blk),
    LOFIDLY(10blk), SLAPBACK, TREMDLY(8blk), APANDLY, TAPEECH3`
  - Synth/bass grit: `MONOSYN(MonoSynth), Z_TRON, BASSMUFF, BASSDRV,
    B_SYNTH, B_ATWAH(BaAutoWah), B_OCTAVE, B_PITCH(6blk)`
  - Filter/dynamics/util: `RESONS, COMBFLTR, M_COMP, RACKCOMP, LIMITER,
    LINESEL, DEFRET`
- Result: **135 patches, 631/778 blocks (147 free)**, re-parsed clean,
  byte-identity of injected effects verified after round-trip.
- Kept stock acid staples already in G1on: `T_SCREAM, GRTMUFF, FUZZSML,
  SQUEAK, GOVERNOR, AUTOWAH, PHASER, SLICER, RINGMOD, BITCRUSH, OCTAVE,
  PTCHSHFT, MNPIT, HPS, TAPEECHO, RVRSDLY, PLATE, HDHALL, ZNR, COMP`.
- Skipped: `_`-prefixed ghosts in MS-60B tables (e.g. `_SVT.ZDL`) —
  unverified duplicates, left out deliberately.

## Suggested 5-slot patches (TD-3 / acid / DnB)

- Acid: `Comp → GreatMuff → AutoWah → Phaser → AnalogDly`
- Dub techno: `VintageCE → TapeEcho → HD Hall`
- DnB/industrial: `FuzzSmile → BitCrush → Slicer` (+ `ZNR` + `Comp`)
- Reese weight: `B_OCTAVE → BassMuff → BaAutoWah → Limiter`

G1on enforces a DSP budget per patch, not just 5 slots — if a chain is
refused, downgrade `HD Hall → Hall`, `ShimmerRv → Plate`.

## Gain staging (synths clip this pedal)

Input is `-20dBm/470k` (guitar), `AUX IN -10dBm/1k` tolerates synths better.
Start synth at 50–75%, comp first, level last. Mono in → use as insert/send;
true stereo needs an MS-70CDR unit, not firmware.

## Flash order

1. Read `SAFETY.md`. Keep the original updater + `.orig.bak`.
2. Flash via Zoom Effect Manager (effect-data-only, safe) preferred over the
   `MODIFIED .exe`.
3. Never unplug mid-flash. No recovery button on G1on — a torn write bricks it.

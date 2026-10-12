# SYNTH FRANKENSTEIN — G1on as a synth multi-FX (no metal, no drums)

Volca Beats owns drums. This pedal owns space/mod/filter. Plan for a
synth-curated ZDL firmware.

## Delete first (frees ~30–40 blocks)

- All `AMP/CAB` sims (FD COMBO, US BLUES, Marshall/JCM clones…). Voiced for
  guitar pickups, eat the most blocks, wreck synth lows/highs. Dropping all
  also drops the `CMN_DRV.ZDL` requirement.
- All hi-gain `DRIVE`: Metal/MetalWRLD/Thrash/Fuzz/Squeak. Keep 1–2 light
  overdrives that tolerate line level; the rest hard-clip `-20dBm` input.
- `PEDAL` wahs (G1Xon-only), acoustic sims, tuner artefacts.
- Drum `*.RAW` + rhythm tables. `FLST_SEQ` already auto-excluded (+2 blocks).

Keep: Looper (30s), NoiseGate/ZNR, Comp/Limiter (first slot — synths are hot),
EQ.

## Inject (same ZDL family only)

- MS-70CDR: `VintageCE, StereoCho, CoronaCho/Tri, SilkyCho, MirageCho,
  VinFLNGR, CloneCho, HD Hall, Plate, Church, Chamber, GateRev, EarlyRef,
  FilterDly, ModDelay, ReverseDL, TapeEcho, Dual DLY, Pitch DLY`.
- MS-50G Manager 72: `BitCrush, Bomber, MonoSynth, Z-Organ, AutoPan`.
- MS-60B/B1on: bass Comp, bass Chorus/Ensemble, Octave, Slicer, RandomFilter.

Target: ~164 fx @100% (community-proven G1on max), 5-slot chains like
`Comp > Slicer > CoronaCho > TapeEcho > HD Hall`.

## Gain staging (synths clip this pedal)

Input is `-20dBm/470k` (guitar), `AUX IN -10dBm/1k` tolerates synths better.
Start synth at 50–75%, comp first, level last. Mono in → use as insert/send;
true stereo needs an MS-70CDR unit, not firmware.

## Flash order

1. Curate offline here, watch blocks bar + safety popups.
2. Save (keeps `.orig.bak`), export `.ZDL` set.
3. Flash via Zoom Effect Manager (effect-data-only, safe) preferred over the
   `MODIFIED .exe`.

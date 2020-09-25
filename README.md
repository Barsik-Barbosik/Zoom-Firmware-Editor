# Zoom Firmware Editor
Zoom Firmware Editor is a software for modifying guitar/bass multi-effects pedal firmware updater files. It can be used for adding, removing, swapping effects; replacing drum sample sounds.

Later will be added possibitily of modifying drum patterns. 

![Application screenshot](screenshot.png)

There are 2 types of supported pedals:
##### Multi-effect pedals that use ZDL-effect format:
```
ZOOM G1on
ZOOM G1Xon
ZOOM B1on
ZOOM B1Xon
ZOOM MS-50G
ZOOM MS-60B
ZOOM MS-70CDR
```

##### Multi-effect pedals that use ZD2-effect format:
```
ZOOM G3X
ZOOM G3n (currently works with firmware 2.00)
ZOOM G3Xn
ZOOM G5n
ZOOM B3n (currently works with firmware 2.00)
ZOOM AC-2
ZOOM AC-3
ZOOM G1 Four *
ZOOM G1X Four *
ZOOM B1 Four *
ZOOM B1X Four *

* - under construction
```
**New effects injection into Four-series pedals is not ready yet. But it is possible! You can you mungewell's python script to install them: [zoom-zt2](https://github.com/mungewell/zoom-zt2)!**

It seems that both pedal types are not compatible with each other: ZDL-type pedals cannot use ZD2-effects and vice versa.

For example, if your multi-effect processor is "G1on", then you can add effects from "B1on", "MS-50G", "MS-60B" (ZDL). But you can't add effects from "G5n", "AC-2" or "B3n" (ZD2).

##### Not supported pedals:
```
ZOOM G3
ZOOM G5
ZOOM B3
ZOOM MS-100BT
Other discontinued products
```

## Getting Started
Zoom Firmware Editor application is a self-executable jar-file. If you have Java JRE installed, then double-click on "ZoomFirmwareEditor.jar" should launch it.

If you dont have Java installed on your computer, you can download and run "ZoomFirmwareEditor-setup.exe". That setup application will install the compiled Windows executable with embedded JVM (created using Excelsior JET).

**Important notice!** If you are going to inject any **GUITAR** amplifier effect into any **BASS** pedal firmware, then you should inject the file **"CMN_DRV.ZDL"** too! 

Some of unique "MS-50G" effects are not included into it's firmware updater. But there is an official "MS-50G Effect Manager", which includes a password-protected archive with all 173 effects. Password is "fDmnZwm2H3mtL8KX". Thanks to GitHub user UnnoTed! Unpacked ZDL-files can be downloaded from his [repository](https://github.com/UnnoTed/zoom-ms50g/tree/master/efx_1_00).

## Requirements
* Java 8 or later
* Original ZOOM firmware updaters (Windows versions, available from https://www.zoom.co.jp/)

## Technical information
*This chapter may contain some incorrect of false information!*

##### RAW Audio sample format
* Encoding: Signed 16-bit PCM
* Byte order: Little-endian
* Channels: 1 channel (mono)?
* Sample rate: 44100 Hz

##### File table beginning: system info (8 bytes)
* 2nd byte: always "A5"
* 4th byte: "01" (G1on, B1on, MS-50G) or "05" (G3n, G5n, B3n)
* 5th byte may be used as a flag: "FF" = list is use; "00" = list is not in use<br/>
If value is "FF", then 1st and 3rd bytes have maximum value among all 4 lists
* 6th, 7th, 8th bytes are always "FF FF FF"

##### File table item (32 bytes)
* 2 bytes (0-1): address (block nr)
* 2 bytes (2-3): "01 FF"
* 4 bytes (4-7): size
* 12 bytes (8-19): filename, followed by "00"
* 12 bytes (20-31): "FF FF FF FF FF FF FF FF FF FF FF FF"

##### File (effect) content block
Block size is 4096 bytes.<br/>
Structure of content block:
* 2 bytes (0-1): previous address; "FF FF" for the first block
* 2 bytes (2-3): next address; "FF FF" for the last block
* 2 bytes (4-5): data size (in current block)
* 4090 bytes (6-4095): content data; followed by "FF", if data size is lesser than 4090 bytes

##### Effects order
The order of effects can be organized by using FLST_SEQ.ZDT/FLST_SEQ.ZT2 files. In the current version of Zoom Firmware Editor those files are not used (and automatically excluded from the firmware: that gives 2 extra blocks of free space). Effect file order is achieved by sorting the file table. That kind of sorting is limited: effects are still grouped by the effect type.

## TODO
* Add columns: effect type, effect description
* Detect bass firmwares and show notification when CMN_DRV.ZDL file is missing
* Separate file filters for ZDL and ZD2
* Drum pattern editor
* Drum sample sound editor or just a menu for replacing sound files (raw-file replacing is already possible)
* Localization
* Code refactoring?

## Acknowledgments
I would like to thank the Youtube user **compashthefirst** for the idea of that project. The main algorithm of swapping effects on ZOOM pedals is based on his video tutorial.

## License
This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details.

Button icons used in this software are licensed under a Creative Commons Attribution-Share Alike 3.0 License. Ownership and copyright of the icons remains the property of Aha-Soft.

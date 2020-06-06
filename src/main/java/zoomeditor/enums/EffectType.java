package main.java.zoomeditor.enums;

public enum EffectType {
    DYNAMICS((byte) 0x01, "DYNAMICS"),
    FILTER((byte) 0x02, "FILTER"),
    G_DRV((byte) 0x03, "GUITAR DRIVE"),
    B_DRV_0C((byte) 0x0C, "BASS DRIVE"), // MS60B
    B_DRV_0D((byte) 0x0D, "BASS DRIVE"), // MS60B
    B_DRV_14((byte) 0x14, "BASS DRIVE"),
    B_DRV_16((byte) 0x16, "BASS DRIVE"),
    G_AMP((byte) 0x04, "GUITAR AMP"),
    B_AMP((byte) 0x05, "BASS AMP"),
    MODULATION((byte) 0x06, "MODULATION"),
    SFX((byte) 0x07, "SFX"),
    DELAY((byte) 0x08, "DELAY"),
    REVERB((byte) 0x09, "REVERB"),
    PEDAL((byte) 0x0B, "PEDAL");

    private final byte typeByte;
    private final String typeName;

    EffectType(byte typeByte, String description) {
        this.typeByte = typeByte;
        this.typeName = description;
    }

    public byte getTypeByte() {
        return typeByte;
    }

    public String getTypeName() {
        return typeName;
    }
}

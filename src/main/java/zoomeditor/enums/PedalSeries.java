package main.java.zoomeditor.enums;

public enum PedalSeries {
    ZOOM_MS("ZOOM MS Series"), // MS-50G, MS-60B, MS-70CDR
    ZOOM_1("ZOOM 1 Series"),   // G1on, G1Xon, B1on, B1Xon
    ZOOM_G("ZOOM G Series"),   // G3X, G3n, G3Xn, G5n, B3n, G1 Four, G1X Four, B1 Four, B1X Four
    ZOOM_AC("ZOOM AC Series"); // AC-2, AC-3

    private final String value;

    PedalSeries(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

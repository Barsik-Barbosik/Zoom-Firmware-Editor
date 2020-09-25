package main.java.zoomeditor.model;

public class DrumPatternItem {
    public static final int NAME_SIZE = 9;

    char[] name;
    char[][] samples = new char[12][9];
    byte tsigTop;
    byte tsigBottom;
    byte bars;
    int pointer; // Int24ul "pointer"
    byte ending; // const bytes "C0"

    public static int getNameSize() {
        return NAME_SIZE;
    }

    public char[] getName() {
        return name;
    }

    public void setName(char[] name) {
        this.name = name;
    }

    public char[][] getSamples() {
        return samples;
    }

    public void setSamples(char[][] samples) {
        this.samples = samples;
    }

    public byte getTsigTop() {
        return tsigTop;
    }

    public void setTsigTop(byte tsigTop) {
        this.tsigTop = tsigTop;
    }

    public byte getTsigBottom() {
        return tsigBottom;
    }

    public void setTsigBottom(byte tsigBottom) {
        this.tsigBottom = tsigBottom;
    }

    public byte getBars() {
        return bars;
    }

    public void setBars(byte bars) {
        this.bars = bars;
    }

    public int getPointer() {
        return pointer;
    }

    public void setPointer(int pointer) {
        this.pointer = pointer;
    }

    public byte getEnding() {
        return ending;
    }

    public void setEnding(byte ending) {
        this.ending = ending;
    }

}

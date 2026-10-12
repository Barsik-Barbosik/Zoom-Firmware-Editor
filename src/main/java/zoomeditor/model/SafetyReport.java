package main.java.zoomeditor.model;

import java.util.ArrayList;
import java.util.List;

/** Result of a pre-mutation safety check. Blocking errors abort, warnings need confirm. */
public class SafetyReport {
    private final List<String> errors = new ArrayList<>();
    private final List<String> warnings = new ArrayList<>();
    private String firmwareFamily = "UNKNOWN";
    private String patchFamily = "UNKNOWN";

    public void addError(String e) { errors.add(e); }
    public void addWarning(String w) { warnings.add(w); }
    public List<String> getErrors() { return errors; }
    public List<String> getWarnings() { return warnings; }
    public boolean isBlocked() { return !errors.isEmpty(); }
    public boolean hasWarnings() { return !warnings.isEmpty(); }
    public String getFirmwareFamily() { return firmwareFamily; }
    public void setFirmwareFamily(String f) { this.firmwareFamily = f; }
    public String getPatchFamily() { return patchFamily; }
    public void setPatchFamily(String p) { this.patchFamily = p; }

    public String summary() {
        StringBuilder sb = new StringBuilder();
        for (String e : errors) sb.append("BLOCKED: ").append(e).append("\n");
        for (String w : warnings) sb.append("WARNING: ").append(w).append("\n");
        return sb.toString().trim();
    }
}

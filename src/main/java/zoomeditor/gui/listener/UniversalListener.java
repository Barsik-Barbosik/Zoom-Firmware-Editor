package main.java.zoomeditor.gui.listener;

import main.java.zoomeditor.controller.ApplicationController;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class UniversalListener implements ActionListener {
    public enum Action {OPEN_FIRMWARE, SAVE_FIRMWARE, EXTRACT_PATCH, INJECT_PATCH, MOVE_UP, MOVE_DOWN, REMOVE_PATCH}

    private final ApplicationController applicationController = ApplicationController.getInstance();
    private final Action action;

    public UniversalListener(Action action) {
        this.action = action;
    }

    public void actionPerformed(ActionEvent e) {
        switch (action) {
            case OPEN_FIRMWARE:
                applicationController.showOpenFirmwareDialog();
                break;
            case SAVE_FIRMWARE:
                applicationController.showSaveFirmwareDialog();
                break;
            case EXTRACT_PATCH:
                applicationController.showSavePatchDialog();
                break;
            case INJECT_PATCH:
                applicationController.showOpenPatchDialog();
                break;
            case REMOVE_PATCH:
                applicationController.removePatch();
                break;
            case MOVE_UP:
                applicationController.moveUpOrDown(true);
                break;
            case MOVE_DOWN:
                applicationController.moveUpOrDown(false);
                break;
        }
    }

}

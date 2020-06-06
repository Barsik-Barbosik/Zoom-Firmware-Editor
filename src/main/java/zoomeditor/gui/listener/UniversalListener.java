package main.java.zoomeditor.gui.listener;

import main.java.zoomeditor.controller.ApplicationController;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class UniversalListener implements ActionListener {
    public enum Action {OPEN_FIRMWARE, SAVE_FIRMWARE, DRUM_EDITOR, EXTRACT_EFFECT, INJECT_EFFECT, MOVE_UP, MOVE_DOWN, REMOVE_EFFECT}

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
            case DRUM_EDITOR:
                applicationController.startDrumEditor();
                break;
            case EXTRACT_EFFECT:
                applicationController.showSaveEffectDialog();
                break;
            case INJECT_EFFECT:
                applicationController.showOpenEffectDialog();
                break;
            case REMOVE_EFFECT:
                applicationController.removeEffect();
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

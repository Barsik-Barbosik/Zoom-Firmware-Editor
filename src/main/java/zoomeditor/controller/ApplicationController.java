package main.java.zoomeditor.controller;

import main.java.ZoomFirmwareEditor;
import main.java.zoomeditor.gui.AppWindow;
import main.java.zoomeditor.gui.MainPanel;
import main.java.zoomeditor.model.Firmware;
import main.java.zoomeditor.model.Effect;
import main.java.zoomeditor.service.FirmwareService;
import main.java.zoomeditor.service.EffectService;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ApplicationController {
    private static volatile ApplicationController instance = null;
    private final FirmwareService firmwareService;
    private final EffectService effectService;
    private static final Logger log = Logger.getLogger(ApplicationController.class.getName());
    private AppWindow appWindow;
    private MainPanel mainPanel;
    private File firmwareFile;
    private Firmware firm;
    private final JFileChooser fc = new JFileChooser(
            ZoomFirmwareEditor.getProperty("defaultPath", System.getProperty("user.dir")));
    private final static String MODIFIED_FILE_PREFIX = "MODIFIED ";

    private ApplicationController() {
        firmwareService = FirmwareService.getInstance();
        effectService = EffectService.getInstance();
    }

    public static ApplicationController getInstance() {
        if (instance == null) {
            synchronized (ApplicationController.class) {
                if (instance == null) {
                    instance = new ApplicationController();
                }
            }
        }
        return instance;
    }

    public static void createAndShowGUI() {
        try {
            getInstance().initAppWindow();
        } catch (Exception e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private void initAppWindow() {
        mainPanel = new MainPanel();
        appWindow = new AppWindow(mainPanel);
        appWindow.setVisible(true);
    }

    /**
     * Shows firmware selection dialog.
     */
    public void showOpenFirmwareDialog() {
        try {
            fc.setDialogTitle(ZoomFirmwareEditor.getMessage("openFirmwareTitle"));
            fc.resetChoosableFileFilters();
            fc.setFileFilter(new FileNameExtensionFilter(ZoomFirmwareEditor.getMessage("exeFileFilter"),
                    "exe"));
            int returnVal = fc.showOpenDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                firmwareFile = fc.getSelectedFile();
                updateTitle();
                firm = firmwareService.initFirmware(firmwareFile);
                appWindow.setMainCard();
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, e.getMessage(), e);
            firm = null;
            JOptionPane.showMessageDialog(null,
                    ZoomFirmwareEditor.getMessage("invalidFirmwareError"),
                    ZoomFirmwareEditor.getMessage("errorTitle"),
                    JOptionPane.ERROR_MESSAGE);
        }
        updateGuiElements();
    }

    /**
     * Shows firmware "Save as" dialog.
     */
    public void showSaveFirmwareDialog() {
        fc.setDialogTitle(ZoomFirmwareEditor.getMessage("saveFirmwareTitle"));
        fc.resetChoosableFileFilters();
        String name = firmwareFile.getName()
                .startsWith(MODIFIED_FILE_PREFIX) ? firmwareFile.getName() : MODIFIED_FILE_PREFIX + firmwareFile.getName();
        fc.setSelectedFile(new File(name));
        int returnVal = fc.showSaveDialog(appWindow);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            boolean isSuccessfulSaving = firmwareService.saveModifiedFirmwareFile(firm,
                    Paths.get(fc.getSelectedFile().getAbsolutePath()));
            if (isSuccessfulSaving) {
                firmwareFile = fc.getSelectedFile();
                updateTitle();
            } else {
                JOptionPane.showMessageDialog(appWindow, ZoomFirmwareEditor.getMessage("firmwareSaveError"),
                        ZoomFirmwareEditor.getMessage("errorTitle"), JOptionPane.ERROR_MESSAGE);
            }
            updateGuiElements();
        }
    }

    /**
     * Shows the file saving dialog and performs the save action for each selected effect.
     */
    public void showSaveEffectDialog() {
        JTable table = mainPanel.getTable();
        if (table.getSelectedRowCount() > 0) {
            fc.setDialogTitle(ZoomFirmwareEditor.getMessage("saveEffectTitle"));
            fc.resetChoosableFileFilters();

            for (int i : table.getSelectedRows()) {
                String fileName = (String) table.getValueAt(i, 0);
                fc.setSelectedFile(new File(fileName));
                int returnVal = fc.showSaveDialog(appWindow);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    boolean isSuccessfulSaving = effectService.saveEffectFile(
                            firm, fileName, Paths.get(fc.getSelectedFile().getAbsolutePath()));
                    if (!isSuccessfulSaving) {
                        JOptionPane.showMessageDialog(appWindow, ZoomFirmwareEditor.getMessage("effectSaveError"),
                                ZoomFirmwareEditor.getMessage("errorTitle"), JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        } else {
            JOptionPane.showMessageDialog(appWindow, ZoomFirmwareEditor.getMessage("effectsAreNotSelected"),
                    ZoomFirmwareEditor.getMessage("warningTitle"), JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Shows the effect selection dialog and then calls the injectEffect() method.
     */
    public void showOpenEffectDialog() {
        fc.setDialogTitle(ZoomFirmwareEditor.getMessage("openEffectTitle"));
        fc.resetChoosableFileFilters();
        fc.setFileFilter(new FileNameExtensionFilter(ZoomFirmwareEditor.getMessage("zdlAndRawFileFilter"),
                "zdl", "zd2", "raw"));
        fc.setSelectedFile(new File("")); // clears selection
        int returnVal = fc.showOpenDialog(appWindow);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File effectFile = fc.getSelectedFile();
            if (effectFile != null) {
                injectEffect(effectFile);
            } else {
                JOptionPane.showMessageDialog(appWindow, ZoomFirmwareEditor.getMessage("effectIsNotSelected"),
                        ZoomFirmwareEditor.getMessage("errorTitle"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Injects an effect into the current firmware.
     *
     * @param effectFile effect file
     */
    private void injectEffect(File effectFile) {
        if (firm != null && firm.getEffects() != null) {
            try {
                Effect effect = effectService.makeEffectFromFile(effectFile);
                firmwareService.injectEffect(firm, effect, true);
            } catch (Exception e) {
                log.log(Level.SEVERE, e.getMessage(), e);
                JOptionPane.showMessageDialog(appWindow, e.getMessage(),
                        ZoomFirmwareEditor.getMessage("errorTitle"), JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        updateGuiElements();
        mainPanel.setScrollBarMaximum();
    }

    /**
     * Method changes the file order in the firmware: moves selected effect up or down.
     *
     * @param isUp if true, then direction is "up"
     */
    public void moveUpOrDown(boolean isUp) {
        if (mainPanel.getTable().getSelectedRowCount() == 1) {
            int scrollBarValue = mainPanel.getScrollBarValue();
            int selectedRow = mainPanel.getTable().getSelectedRow();
            String fileName = (String) mainPanel.getTable().getValueAt(selectedRow, 0);
            firmwareService.moveEffectUpOrDown(firm, fileName, isUp);
            updateGuiElements();
            if (isUp && selectedRow > 0) {
                mainPanel.getTable().setRowSelectionInterval(selectedRow - 1, selectedRow - 1);
            } else if (!isUp && selectedRow < firm.getEffects().size() - 1) {
                mainPanel.getTable().setRowSelectionInterval(selectedRow + 1, selectedRow + 1);
            } else {
                mainPanel.getTable().setRowSelectionInterval(selectedRow, selectedRow);
            }
            mainPanel.setScrollBarValue(scrollBarValue);
        } else {
            JOptionPane.showMessageDialog(appWindow, ZoomFirmwareEditor.getMessage("selectOneEffect"),
                    ZoomFirmwareEditor.getMessage("warningTitle"), JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Removes selected effects from the current firmware.
     */
    public void removeEffect() {
        JTable table = mainPanel.getTable();
        if (table.getSelectedRowCount() > 0) {
            ArrayList<String> filesToRemove = new ArrayList<>();
            for (int i : table.getSelectedRows()) {
                String fileName = (String) table.getValueAt(i, 0);
                filesToRemove.add(fileName);
            }
            firmwareService.removeEffectFile(firm, filesToRemove);
            updateGuiElements();
        } else {
            JOptionPane.showMessageDialog(appWindow, ZoomFirmwareEditor.getMessage("effectsAreNotSelected"),
                    ZoomFirmwareEditor.getMessage("warningTitle"), JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Updates current firmware name in the application window title.
     */
    private void updateTitle() {
        appWindow.setTitle(firmwareFile.getName() + " \u2014 " + ZoomFirmwareEditor.getProperty("title"));
    }

    /**
     * Updates main GUI elements:<br/>
     * - enables/disables buttons<br/>
     * - updates effects table<br/>
     * - updates used blocks bar.
     */
    private void updateGuiElements() {
        if (firm != null) {
            ArrayList<Effect> effects = firm.getEffects();
            if (effects != null) {
                mainPanel.enableControls(true);
                mainPanel.updateBlocksBar(firmwareService.getUsedBlocksCount(firm),
                        firmwareService.getTotalBlocksCount(firm));
                mainPanel.updateEffectTable(effects);
                return;
            }
        }
        mainPanel.enableControls(false);
        mainPanel.updateBlocksBar(0, 0);
        mainPanel.updateEffectTable(null);
    }

}

package main.java.zoomeditor.gui;

import main.java.ZoomFirmwareEditor;
import main.java.zoomeditor.gui.listener.UniversalListener;
import main.java.zoomeditor.gui.tablemodel.EffectTableModel;
import main.java.zoomeditor.model.Effect;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import java.util.ArrayList;

public class MainPanel extends JPanel {

    private JTable table;
    private final JScrollPane scrollPane;
    private final JProgressBar blocksBar;
    private final JButton btnDrumEditor;
    private final JButton btnExtract;
    private final JButton btnInject;
    private final JButton btnMoveUp;
    private final JButton btnMoveDown;
    private final JButton btnRemove;
    private final JButton btnSaveFirmware;

    private final static int BUTTON_WIDTH = "true".equalsIgnoreCase(ZoomFirmwareEditor.getProperty("useWindowsLookAndFeel")) ? 110 : 125;

    public MainPanel() {
        scrollPane = new JScrollPane();

        JButton btnOpenFirmware = GuiFactory.getButton(ZoomFirmwareEditor.getMessage("openFirmwareButton"),
                "open.png", ZoomFirmwareEditor.getMessage("openFirmwareTooltip"));
        btnOpenFirmware.addActionListener(new UniversalListener(UniversalListener.Action.OPEN_FIRMWARE));

        btnSaveFirmware = GuiFactory.getButton(ZoomFirmwareEditor.getMessage("saveFirmwareButton"),
                "save.png", ZoomFirmwareEditor.getMessage("saveFirmwareTooltip"));
        btnSaveFirmware.addActionListener(new UniversalListener(UniversalListener.Action.SAVE_FIRMWARE));

        btnDrumEditor = GuiFactory.getButton(ZoomFirmwareEditor.getMessage("drumsButton"),
                "drum_editor.png", ZoomFirmwareEditor.getMessage("drumsTooltip"));
        btnDrumEditor.addActionListener(new UniversalListener(UniversalListener.Action.DRUM_EDITOR));

        btnExtract = GuiFactory.getButton(ZoomFirmwareEditor.getMessage("extractButton"),
                "extract.png", ZoomFirmwareEditor.getMessage("extractTooltip"));
        btnExtract.addActionListener(new UniversalListener(UniversalListener.Action.EXTRACT_EFFECT));

        btnInject = GuiFactory.getButton(ZoomFirmwareEditor.getMessage("injectButton"),
                "inject.png", ZoomFirmwareEditor.getMessage("injectTooltip"));
        btnInject.addActionListener(new UniversalListener(UniversalListener.Action.INJECT_EFFECT));

        btnMoveUp = GuiFactory.getButton(ZoomFirmwareEditor.getMessage("moveUpButton"),
                "up.png", ZoomFirmwareEditor.getMessage("moveUpTooltip"));
        btnMoveUp.addActionListener(new UniversalListener(UniversalListener.Action.MOVE_UP));

        btnMoveDown = GuiFactory.getButton(ZoomFirmwareEditor.getMessage("moveDownButton"),
                "down.png", ZoomFirmwareEditor.getMessage("moveDownTooltip"));
        btnMoveDown.addActionListener(new UniversalListener(UniversalListener.Action.MOVE_DOWN));

        btnRemove = GuiFactory.getButton(ZoomFirmwareEditor.getMessage("removeButton"),
                "remove.png", ZoomFirmwareEditor.getMessage("removeTooltip"));
        btnRemove.addActionListener(new UniversalListener(UniversalListener.Action.REMOVE_EFFECT));

        blocksBar = new JProgressBar();
        blocksBar.setToolTipText(ZoomFirmwareEditor.getMessage("blocksBarTooltip"));

        JPanel panel = new JPanel();

        GroupLayout groupLayout = new GroupLayout(this);
        groupLayout.setHorizontalGroup(
                groupLayout.createParallelGroup(Alignment.TRAILING)
                        .addGroup(groupLayout.createSequentialGroup()
                                .addGap(6)
                                .addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
                                        .addComponent(blocksBar, GroupLayout.DEFAULT_SIZE, 488, Short.MAX_VALUE)
                                        .addGroup(groupLayout.createSequentialGroup()
                                                .addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 372, Short.MAX_VALUE)
                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                                                        .addComponent(panel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                        .addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
                                                                .addComponent(btnDrumEditor, GroupLayout.DEFAULT_SIZE, BUTTON_WIDTH, Short.MAX_VALUE)
                                                                .addComponent(btnExtract, GroupLayout.DEFAULT_SIZE, BUTTON_WIDTH, Short.MAX_VALUE)
                                                                .addComponent(btnMoveUp, GroupLayout.DEFAULT_SIZE, BUTTON_WIDTH, Short.MAX_VALUE)
                                                                .addComponent(btnMoveDown, GroupLayout.DEFAULT_SIZE, BUTTON_WIDTH, Short.MAX_VALUE)
                                                                .addComponent(btnRemove, GroupLayout.DEFAULT_SIZE, BUTTON_WIDTH, Short.MAX_VALUE)
                                                                .addComponent(btnInject, GroupLayout.DEFAULT_SIZE, BUTTON_WIDTH, Short.MAX_VALUE)))))
                                .addGap(6))
        );
        groupLayout.setVerticalGroup(
                groupLayout.createParallelGroup(Alignment.LEADING)
                        .addGroup(groupLayout.createSequentialGroup()
                                .addGap(6)
                                .addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
                                        .addGroup(groupLayout.createSequentialGroup()
                                                .addComponent(panel, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(ComponentPlacement.RELATED, 160, Short.MAX_VALUE)
                                                .addComponent(btnDrumEditor)
                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                .addComponent(btnExtract)
                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                .addComponent(btnInject)
                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                .addComponent(btnMoveUp)
                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                .addComponent(btnMoveDown)
                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                .addComponent(btnRemove))
                                        .addComponent(scrollPane, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 358, Short.MAX_VALUE))
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(blocksBar, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
                                .addGap(6))
        );

        GroupLayout gl_panel = new GroupLayout(panel);
        gl_panel.setHorizontalGroup(
                gl_panel.createParallelGroup(Alignment.LEADING)
                        .addComponent(btnSaveFirmware, GroupLayout.PREFERRED_SIZE, BUTTON_WIDTH, GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnOpenFirmware, GroupLayout.PREFERRED_SIZE, BUTTON_WIDTH, GroupLayout.PREFERRED_SIZE)
        );
        gl_panel.setVerticalGroup(
                gl_panel.createParallelGroup(Alignment.LEADING)
                        .addGroup(gl_panel.createSequentialGroup()
                                .addComponent(btnOpenFirmware)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(btnSaveFirmware)
                                .addContainerGap(30, Short.MAX_VALUE))
        );
        panel.setLayout(gl_panel);
        setLayout(groupLayout);

        enableControls(false);
    }

    public void enableControls(boolean isEnabled) {
        btnExtract.setEnabled(isEnabled);
        btnInject.setEnabled(isEnabled);
        btnMoveUp.setEnabled(isEnabled);
        btnMoveDown.setEnabled(isEnabled);
        btnRemove.setEnabled(isEnabled);
        btnSaveFirmware.setEnabled(isEnabled);
    }

    public void updateBlocksBar(int used, int total) {
        blocksBar.setStringPainted(true);
        blocksBar.setMaximum(total);
        blocksBar.setValue(used);
        blocksBar.setString("Used: " + used + "/" + total + " blocks");
    }

    public void updateEffectTable(ArrayList<Effect> effects) {
        if (effects == null) {
            scrollPane.setViewportView(new JTable());
            return;
        }
        table = GuiFactory.getTable(new EffectTableModel(effects));
        scrollPane.setViewportView(table);
    }

    public JTable getTable() {
        return table;
    }

    public int getScrollBarValue() {
        return scrollPane.getVerticalScrollBar().getValue();
    }

    public void setScrollBarValue(int val) {
        scrollPane.getVerticalScrollBar().setValue(val);
    }

    public void setScrollBarMaximum() {
        int maxValue = scrollPane.getVerticalScrollBar().getMaximum();
        scrollPane.getVerticalScrollBar().setValue(maxValue);
    }

}
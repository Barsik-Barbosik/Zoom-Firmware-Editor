package main.java.zoomeditor.gui;

import main.java.ZoomFirmwareEditor;
import main.java.zoomeditor.gui.listener.UniversalListener;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;

class DisclaimerPanel extends JPanel {
    private static final String DISCLAIMER = ZoomFirmwareEditor.getMessage("disclaimerText");

    DisclaimerPanel() {
        JLabel lblNewLabel = new JLabel(ZoomFirmwareEditor.getMessage("disclaimerLabel"));
        lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JTextPane txtpnDisclaimerText = new JTextPane();
        txtpnDisclaimerText.setEditable(false);
        txtpnDisclaimerText.setText(DISCLAIMER);

        JPanel panel = new JPanel();
        GroupLayout groupLayout = new GroupLayout(this);
        groupLayout.setHorizontalGroup(
                groupLayout.createParallelGroup(Alignment.TRAILING)
                        .addGroup(groupLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
                                        .addComponent(txtpnDisclaimerText, Alignment.LEADING, GroupLayout.DEFAULT_SIZE,
                                                430, Short.MAX_VALUE)
                                        .addComponent(lblNewLabel, Alignment.LEADING, GroupLayout.DEFAULT_SIZE,
                                                430, Short.MAX_VALUE)
                                        .addComponent(panel, Alignment.LEADING, GroupLayout.DEFAULT_SIZE,
                                                430, Short.MAX_VALUE))
                                .addContainerGap())
        );
        groupLayout.setVerticalGroup(
                groupLayout.createParallelGroup(Alignment.LEADING)
                        .addGroup(groupLayout.createSequentialGroup()
                                .addGap(90)
                                .addComponent(lblNewLabel)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(txtpnDisclaimerText, GroupLayout.DEFAULT_SIZE, 140, Short.MAX_VALUE)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(panel, GroupLayout.PREFERRED_SIZE, 36, GroupLayout.PREFERRED_SIZE)
                                .addGap(90))
        );

        JButton btnOk = GuiFactory.getButton(ZoomFirmwareEditor.getMessage("okAndOpenFirmware"), "ok.png",
                ZoomFirmwareEditor.getMessage("okAndOpenFirmwareTooltip"));
        btnOk.addActionListener(new UniversalListener(UniversalListener.Action.OPEN_FIRMWARE));
        panel.add(btnOk);
        setLayout(groupLayout);
    }
}

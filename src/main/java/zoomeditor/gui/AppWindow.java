package main.java.zoomeditor.gui;

import main.java.ZoomFirmwareEditor;

import javax.swing.*;
import java.awt.*;

public class AppWindow extends JFrame {
    private final CardLayout cl;
    private final JPanel cards;

    public AppWindow(MainPanel mainPanel) {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle(ZoomFirmwareEditor.getProperty("title") + " " + ZoomFirmwareEditor.getProperty("version"));
        Dimension dimension = new Dimension(550, 443);
        setSize(dimension);
        setMinimumSize(dimension);
        setResizable(true);
        setLocationRelativeTo(null);

        cl = new CardLayout();
        cards = new JPanel(cl);
        cards.add(new DisclaimerPanel());
        cards.add(mainPanel);
        add(cards);
        cl.first(cards);
    }

    public void setMainCard() {
        cl.last(cards);
    }

}

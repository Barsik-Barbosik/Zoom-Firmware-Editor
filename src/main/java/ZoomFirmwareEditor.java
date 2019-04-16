package main.java;

import main.java.zoomeditor.controller.ApplicationController;

import javax.swing.*;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ZoomFirmwareEditor {
    private static final String CONFIG_FILE = "main/resources/app.config";
    private static final String MESSAGES_FILE = "main/resources/messages";
    private static Properties properties;
    private static ResourceBundle messages;
    private static final Logger log = Logger.getLogger(ZoomFirmwareEditor.class.getName());

    public static void main(String[] args) {
        System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tF %1$tT %4$s %2$s(): %5$s%6$s%n"); // set log output into single line
        try {
            properties = new Properties();
            properties.load(ZoomFirmwareEditor.class.getClassLoader().getResourceAsStream(CONFIG_FILE));
            messages = ResourceBundle.getBundle(MESSAGES_FILE, Locale.ROOT);
        } catch (IOException e) {
            e.printStackTrace();
            log.log(Level.SEVERE, e.getMessage(), e);
            return;
        }

        if ("true".equalsIgnoreCase(getProperty("useWindowsLookAndFeel"))) {
            try {
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            } catch (Exception e) {
                log.info(e.getMessage());
                properties.setProperty("useWindowsLookAndFeel", "false");
            }
        }

        SwingUtilities.invokeLater(ApplicationController::createAndShowGUI);
    }

    public static String getProperty(String key, String defaultValue) {
        String val = getProperty(key);
        return (val == null) ? defaultValue : val;
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    public static String getMessage(String key) {
        return messages.getString(key);
    }

}

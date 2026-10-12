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
        ensureLoaded();

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

    /**
     * Loads config + messages on first use, so library/headless callers
     * (which never enter main()) don't NPE in getProperty()/getMessage().
     * Missing resources fall back to empty defaults instead of crashing.
     */
    public static synchronized void ensureLoaded() {
        if (properties == null) {
            properties = new Properties();
            try {
                java.io.InputStream in = ZoomFirmwareEditor.class.getClassLoader()
                        .getResourceAsStream(CONFIG_FILE);
                if (in != null) {
                    properties.load(in);
                } else {
                    log.warning("Config not found on classpath: " + CONFIG_FILE);
                }
            } catch (IOException e) {
                log.log(Level.SEVERE, e.getMessage(), e);
            }
        }
        if (messages == null) {
            try {
                messages = ResourceBundle.getBundle(MESSAGES_FILE, Locale.ROOT);
            } catch (Exception e) {
                log.warning("Messages bundle not found: " + MESSAGES_FILE);
            }
        }
    }

    public static String getProperty(String key, String defaultValue) {
        String val = getProperty(key);
        return (val == null) ? defaultValue : val;
    }

    public static String getProperty(String key) {
        ensureLoaded();
        return properties.getProperty(key);
    }

    public static String getMessage(String key) {
        ensureLoaded();
        if (messages == null) {
            return key;
        }
        try {
            return messages.getString(key);
        } catch (Exception e) {
            return key;
        }
    }

}

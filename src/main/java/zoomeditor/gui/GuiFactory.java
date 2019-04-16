package main.java.zoomeditor.gui;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.Objects;
import java.util.logging.Logger;

class GuiFactory {
    private final static String ICONS_PATH = "main/resources/icons/";
    private static final Logger log = Logger.getLogger(GuiFactory.class.getName());

    static JButton getButton(String text, String iconName, String tooltip) {
        JButton button = new JButton(text);
        button.setToolTipText(tooltip);
        addButtonIcon(button, iconName);
        return button;
    }

    private static void addButtonIcon(JButton button, String iconName) {
        try {
            button.setIcon(new ImageIcon(Objects.requireNonNull(
                    GuiFactory.class.getClassLoader().getResource(ICONS_PATH + iconName))));
            button.setMargin(new Insets(2, 4, 2, 2));
            button.setIconTextGap(8);
            button.setHorizontalAlignment(SwingConstants.LEFT);
        } catch (Exception ex) {
            log.severe("Icon image loading failed: " + iconName);
        }
    }

    static JTable getTable(TableModel tableModel) {
        JTable table = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component component = super.prepareRenderer(renderer, row, column);
                if (!component.getBackground().equals(getSelectionBackground())) {
                    Color color = new Color(237, 243, 254);
                    component.setBackground(row % 2 == 0 ? Color.WHITE : color);
                }
                return component;
            }
        };
        table.getColumnModel().getColumn(0).setPreferredWidth(120);
        table.getColumnModel().getColumn(1).setPreferredWidth(120);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.setCellSelectionEnabled(false);
        table.setColumnSelectionAllowed(false);
        table.setRowSelectionAllowed(true);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.revalidate();
        table.repaint();
        return table;
    }

}

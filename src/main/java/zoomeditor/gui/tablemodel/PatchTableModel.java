package main.java.zoomeditor.gui.tablemodel;

import main.java.ZoomFirmwareEditor;
import main.java.zoomeditor.model.Patch;
import main.java.zoomeditor.service.PatchService;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.util.List;

public class PatchTableModel implements TableModel {

    private final List<Patch> patches;

    public PatchTableModel(List<Patch> patches) {
        this.patches = patches;
    }

    @Override
    public int getRowCount() {
        return patches.size();
    }

    @Override
    public int getColumnCount() {
        return 5;
    }

    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return ZoomFirmwareEditor.getMessage("fileNameColumn");
            case 1:
                return ZoomFirmwareEditor.getMessage("patchNameColumn");
            case 2:
                return ZoomFirmwareEditor.getMessage("typeColumn");
            case 3:
                return ZoomFirmwareEditor.getMessage("sizeColumn");
            case 4:
                return ZoomFirmwareEditor.getMessage("blocksColumn");
        }
        return "";
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        //return getValueAt(0, columnIndex).getClass();
        if (columnIndex == 3 || columnIndex == 4) {
            return Number.class;
        }
        return String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Patch bean = patches.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return bean.getFileName();
            case 1:
                return bean.getName();
            case 2:
                return PatchService.getEffectTypeName(bean.getType(), bean.getFileName());
            case 3:
                return bean.getSize();
            case 4:
                return PatchService.calculatePatchBlocksCount(bean.getSize());
        }
        return "";
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    }

    @Override
    public void addTableModelListener(TableModelListener l) {
    }

    @Override
    public void removeTableModelListener(TableModelListener l) {
    }

}
/*
 * Copyright (C) 2016 thedathoudarya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.clough.android.adbv.util;

import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 *
 * @author Thedath Oudarya
 */
public class TableEditor {

    private static final TableEditor TABLE_EDITOR = new TableEditor();

    private TableEditor() {
    }

    public static TableEditor getInstance() {
        return TABLE_EDITOR;
    }

    /*
     for (int column = 0; column < table.getColumnCount(); column++)
     {
     TableColumn tableColumn = table.getColumnModel().getColumn(column);
     int preferredWidth = tableColumn.getMinWidth();
     int maxWidth = tableColumn.getMaxWidth();
 
     for (int row = 0; row < table.getRowCount(); row++)
     {
     TableCellRenderer cellRenderer = table.getCellRenderer(row, column);
     Component c = table.prepareRenderer(cellRenderer, row, column);
     int width = c.getPreferredSize().width + table.getIntercellSpacing().width;
     preferredWidth = Math.max(preferredWidth, width);
 
     //  We've exceeded the maximum width, no need to check other rows
 
     if (preferredWidth >= maxWidth)
     {
     preferredWidth = maxWidth;
     break;
     }
     }
 
     tableColumn.setPreferredWidth( preferredWidth );
     }    
     */
    public void adjustTable(Component tableContainer, JTable table) {
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        DefaultTableColumnModel defColumnModel = (DefaultTableColumnModel) table.getColumnModel();
        int columnCount = table.getColumnCount();
        int[] columnWidths = new int[columnCount];
        int totalWidth = 0;
        for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
            TableColumn tableColumn = defColumnModel.getColumn(columnIndex);
            TableCellRenderer headerRenderer = tableColumn.getHeaderRenderer();
            if (headerRenderer == null) {
                headerRenderer = table.getTableHeader().getDefaultRenderer();
            }
            int minWidth = tableColumn.getMinWidth();
            int headerWidth = headerRenderer.getTableCellRendererComponent(table, tableColumn.getHeaderValue(), false, false, -1, columnIndex).getPreferredSize().width;
            
            int finalColumnWidth = Math.max(minWidth, headerWidth);
            for (int rowIndex = 0; rowIndex < table.getRowCount(); rowIndex++) {
                int rowWidth = table.prepareRenderer(table.getCellRenderer(rowIndex, columnIndex), rowIndex, columnIndex).getPreferredSize().width;
                finalColumnWidth = Math.max(rowWidth, finalColumnWidth);
//                Object cellValue = table.getValueAt(rowIndex, columnIndex);
//                try {
//                    long longValue = (Long) cellValue;
//                    cellTextField.setHorizontalAlignment(JTextField.RIGHT);
//                } catch (Exception e0) {
//                    try {
//                        double doubleValue = (Double) cellValue;
//                        cellTextField.setHorizontalAlignment(JTextField.RIGHT);
//                    } catch (Exception e1) {
//                        try {
//                            boolean doubleValue = (Boolean) cellValue;
//                            cellTextField.setHorizontalAlignment(JTextField.CENTER);
//                        } catch (Exception e2) {
//                            cellTextField.setHorizontalAlignment(JTextField.LEADING);
//                        }
//                    }
//                }
            }
            columnWidths[columnIndex] = finalColumnWidth;
            totalWidth += columnWidths[columnIndex];
        }
        int containerWidth = tableContainer.getWidth();
        if (totalWidth < containerWidth) {
            TableColumnModel columnModel = table.getColumnModel();
            int rest = containerWidth - totalWidth;
            int additionalWidth = rest / columnCount;
            int extraWidth = rest % columnCount;
            int maxWidthColumnIndex = 0;
            int maxColumnWidth = 0;
            for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
                int columnWidth = columnWidths[columnIndex];
                if (maxColumnWidth < columnWidth) {
                    maxColumnWidth = columnWidth;
                    maxWidthColumnIndex = columnIndex;
                }
            }
            for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
                columnModel.getColumn(columnIndex).setPreferredWidth(columnWidths[columnIndex] + additionalWidth + (columnIndex == maxWidthColumnIndex ? extraWidth : 0));
            }
        }
    }

}

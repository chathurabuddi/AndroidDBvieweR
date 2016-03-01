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
package com.clough.android.adbv.view;

import com.clough.android.adbv.controller.RowController;
import com.clough.android.adbv.model.Row;
import com.clough.android.adbv.util.TableColumnAdjuster;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author thedathoudarya
 */
public class UpdateTableDialog extends javax.swing.JDialog {

    private final MainFrame mainFrame;
    private final String outputResult;
    private WaitingDialog waitingDialog;
    private final DefaultTableModel defaultTableModel;
    private final TableColumnAdjuster tableColumnAdjuster;
    private List<Row> rowList;
    private List<Row> rowsToChange;
    private RowController rowController;
    private final Object[] columnNames;
    private final String tableName;

    public UpdateTableDialog(java.awt.Frame parent, boolean modal, String outputResult, String tableName, Object[] columnNames) {
        super(parent, modal);
        initComponents();
        this.outputResult = outputResult;
        this.tableName = tableName;
        this.columnNames = columnNames;
        this.mainFrame = (MainFrame) parent;
        setTitle("Update table `" + tableName + "`");
        setLocationRelativeTo(null);
        resultTable.setModel(new DefaultTableModel() {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

        });
        resultTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        defaultTableModel = (DefaultTableModel) resultTable.getModel();
        for (Object columnName : columnNames) {
            defaultTableModel.addColumn(columnName);
        }
        tableColumnAdjuster = new TableColumnAdjuster(resultTable);

        processResult();
    }

    private synchronized void processResult() {
        try {
            final JSONArray jsonArray = new JSONArray(outputResult);
            final int jsonObjectLength = jsonArray.length();

            new SwingWorker<Void, Void>() {

                boolean columnsFound = false;

                @Override
                protected Void doInBackground() {
                    rowList = new ArrayList<Row>();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                    }
                    try {
                        for (int i = 0; i < jsonObjectLength; i++) {
                            waitingDialog.incrementProgressBar();
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            Object[] rowData = new Object[defaultTableModel.getColumnCount()];
                            for (int j = 0; j < resultTable.getColumnCount(); j++) {
                                String cellValue = String.valueOf(jsonObject.get(resultTable.getColumnName(j))).replaceAll("\n", "").replaceAll("\t", " ");
                                rowData[j] = cellValue;
                            }
                            defaultTableModel.addRow(rowData);
                            rowList.add(new Row(i, rowData, Row.DEFAULT));
                        }
                    } catch (Exception e) {
                        return null;
                    }
                    return null;
                }

                @Override
                protected void done() {
                    tableColumnAdjuster.adjustColumns();                  
                    rowController = RowController.getPreparedRowController(rowList);
                    closeProgressDialog();
                    resultTable.setRowSelectionAllowed(false);
                    rowsToChange = new ArrayList<Row>();
                    rowsToChange.addAll(rowList);
                }

            }.execute();
            showProgressDialog(false, jsonObjectLength, "Processing " + jsonObjectLength + " fields");
        } catch (JSONException ex) {
            JOptionPane.showMessageDialog(null, outputResult, "Result error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showProgressDialog(boolean interminidate, int maxProgressValue, String message) {
        waitingDialog = new WaitingDialog(null, interminidate, maxProgressValue, message);
        waitingDialog.setVisible(true);
    }

    private void closeProgressDialog() {
        waitingDialog.setVisible(false);
        waitingDialog.dispose();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        resultTable = new javax.swing.JTable();
        applyChangesButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setAutoscrolls(true);

        resultTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        resultTable.getTableHeader().setReorderingAllowed(false);
        resultTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                resultTableMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(resultTable);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1)
        );

        applyChangesButton.setText("Apply changes");
        applyChangesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyChangesButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 350, Short.MAX_VALUE)
                        .addComponent(applyChangesButton))
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(applyChangesButton)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void applyChangesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_applyChangesButtonActionPerformed
        // TODO add your handling code here:
        List<Row> rowsToInsert = rowController.getRowsToInsert();
        List<Row> rowsToUpdate = rowController.getRowsToUpdate();
        List<Row> rowsToDelete = rowController.getRowsToDelete();
        int insertCount = rowsToInsert.size();
        int updateCount = rowsToUpdate.size();
        int deleteCount = rowsToDelete.size();
        if (insertCount > 0 || updateCount > 0 || deleteCount > 0) {
            String message
                    = "Inserting row count " + insertCount + "\n"
                    + "Updating row count " + updateCount + "\n"
                    + "Deleting row count " + deleteCount + "\n"
                    + "Are you sure you want apply this changes?";
            if (JOptionPane.showConfirmDialog(null, message, "Applying changes", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                mainFrame.setTableChangeList(rowsToChange, rowsToInsert, rowsToUpdate, rowsToDelete);
                dispose();
            }
        } else {
            JOptionPane.showMessageDialog(null, "No changes to apply", "Applying changes", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        }
    }//GEN-LAST:event_applyChangesButtonActionPerformed

    private void resultTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_resultTableMouseClicked
        // TODO add your handling code here:
        if (evt.getButton() == MouseEvent.BUTTON3) {
            int rowIndex;
            if ((rowIndex = validateRowSelection()) != -1) {                      
                showTableChangeOptionMenu(evt.getLocationOnScreen().x - getX(), evt.getLocationOnScreen().y - getY(), rowIndex);
            }
        }
    }//GEN-LAST:event_resultTableMouseClicked

    private void showTableChangeOptionMenu(int x, int y, final int rowIndex) {
        JPopupMenu tableChangeOptionPopupMenu = new JPopupMenu();
        JMenuItem newRowMenuItem = new JMenuItem("Add row");
        newRowMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                new AddUpdateRowDialog(defaultTableModel, rowController, columnNames, null, -1).setVisible(true);
            }
        });
        JMenuItem updateRowMenuItem = new JMenuItem("Update row");
        updateRowMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Object[] row = new Object[columnNames.length];
                for (int columnIndex = 0; columnIndex < row.length; columnIndex++) {
                    row[columnIndex] = defaultTableModel.getValueAt(rowIndex, columnIndex);
                }
                new AddUpdateRowDialog(defaultTableModel, rowController, columnNames, row, rowIndex).setVisible(true);
                tableColumnAdjuster.adjustColumns();
            }
        });
        JMenuItem removeRowMenuItem = new JMenuItem("Remove row");
        removeRowMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (JOptionPane.showConfirmDialog(null, "Are you sure you want to remove this row?", "Remove row", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    defaultTableModel.removeRow(rowIndex);
                    rowController.removeRow(rowIndex);
                    tableColumnAdjuster.adjustColumns();
                }
            }
        });
        tableChangeOptionPopupMenu.add(newRowMenuItem);
        tableChangeOptionPopupMenu.add(updateRowMenuItem);
        tableChangeOptionPopupMenu.add(removeRowMenuItem);
        tableChangeOptionPopupMenu.show(this, x, y);
    }

    private int validateRowSelection() {
        int[] selectedRows = resultTable.getSelectedRows();
        int rowCount = selectedRows.length;
        if (rowCount > 0) {
            return selectedRows[rowCount - 1];
        } else {
            JOptionPane.showMessageDialog(null, "Select a row to remove/update", "Row removing/updating", JOptionPane.WARNING_MESSAGE);
            return -1;
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton applyChangesButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable resultTable;
    // End of variables declaration//GEN-END:variables
}

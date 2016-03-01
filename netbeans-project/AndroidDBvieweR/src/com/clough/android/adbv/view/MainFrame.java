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

import com.clough.android.adbv.exception.HistoryManagerException;
import com.clough.android.adbv.exception.IOManagerException;
import com.clough.android.adbv.manager.HistoryManager;
import com.clough.android.adbv.manager.IOManager;
import com.clough.android.adbv.model.Row;
import com.clough.android.adbv.util.ValueHolder;
import com.clough.android.adbv.util.TableColumnAdjuster;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.SwingWorker;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import wagu.Board;
import wagu.Table;

/**
 *
 * @author thedathoudarya
 */
public class MainFrame extends javax.swing.JFrame {

    private IOManager ioManager;
    private HistoryManager historyManager;
    private String deviceName;
    private String applicationID;
    private String inputQuery;
    private String outputResult;
    private String outputResultAsTextTable;
    private WaitingDialog waitingDialog;
    private String databaseName;
    private JSONArray tableNameJSONArray;
    private String selectedTreeNodeValue;
    private String[] tables;
    private String[] queries;
    private String[][] columns;
    private String[][][] columnInfos;
    private Map<String, String> tableQueryList;
    private List<Row> rowsToChange = null;
    private List<Row> rowsToInsert = null;
    private List<Row> rowsToUpdate = null;
    private List<Row> rowsToDelete = null;
    private DefaultTableModel defaultTableModel;
    private TableColumnAdjuster tableColumnAdjuster;
    private ArrayList<String[]> currentHistoryList;

    public MainFrame() {
        initComponents();
        setIconImage(ValueHolder.Icons.APPLICATION.getImage());
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        Dimension screenDimension = Toolkit.getDefaultToolkit().getScreenSize();
        double windowWidth = screenDimension.getWidth();
        double windowHeight = screenDimension.getHeight();
        int frameWidth = (int) (windowWidth * (3d / 4d));
        int frameHeight = (int) (windowHeight * (3d / 4d));
        setMinimumSize(new Dimension(frameWidth, frameHeight));
        setLocation((int) ((windowWidth - frameWidth) / 2d), (int) ((windowHeight - frameHeight) / 2d));

        int deviderSize = (int) (frameWidth * (1d / 100d));

        int deviderLocationForSpliter0 = (int) (frameWidth / 5d);
        splitPane0.setDividerLocation(deviderLocationForSpliter0);

        int deviderLocationForSpliter1 = (int) (frameWidth * (6d / 7d));
        splitPane1.setDividerLocation(deviderLocationForSpliter1);

        int subDeviderLocation = (int) (frameHeight / 6d);
        splitPane2.setDividerLocation(subDeviderLocation);

        queryHistoryContainerPanel.setMinimumSize(new Dimension(deviderLocationForSpliter0, 0));
        queryRootConatinerPanel.setMinimumSize(new Dimension(0, subDeviderLocation));

        queryingTextArea.requestFocus();

        resultTable.setModel(new DefaultTableModel() {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

        });

        defaultTableModel = (DefaultTableModel) resultTable.getModel();
        resultTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        tableColumnAdjuster = new TableColumnAdjuster(resultTable);

        historyContainingPanel.setLayout(new GridLayout(0, 1));

    }

    public MainFrame(IOManager ioManager, HistoryManager historyManager) {
        this();
        this.ioManager = ioManager;
        this.historyManager = historyManager;
        ioManager.addConnectionLostListener(new IOManager.ConnectionLostListener() {
            @Override
            public void onDisconnect() {
                showDeviceDisconnectedDialog();
            }
        });

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                }
                try {
                    applicationID = MainFrame.this.ioManager.getApplicationID();
                    deviceName = MainFrame.this.ioManager.getDeviceName();
                    databaseName = MainFrame.this.ioManager.getDatabaseName();
                    setTitle(ValueHolder.WINDOW_TITLE + " - (" + deviceName + " - " + applicationID + ")");
                } catch (IOManagerException ex) {
                    showDeviceDisconnectedDialog();
                }
                return null;
            }

            @Override
            protected void done() {
                closeProgressDialog();
            }

        }.execute();
        showProgressDialog(true, 0, "Waiting for device/app info");

        refreshDatabase();

        tableInfoTree.setCellRenderer(new DefaultTreeCellRenderer() {

            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) value;
                String nodeString = String.valueOf(dmtn.getUserObject());
                ImageIcon selectedImageIcon = null;
                if (nodeString.equals(databaseName)) {
                    selectedImageIcon = ValueHolder.Icons.DATABASE;
                } else {
                    l:
                    for (int i = 0; i < tables.length; i++) {
                        String tableName = tables[i];
                        if (tableName.equals(nodeString)) {
                            selectedImageIcon = ValueHolder.Icons.TABLE;
                            break;
                        } else if (i == tables.length - 1) {
                            for (int p = 0; p < tables.length; p++) {
                                for (int j = 0; j < columns[p].length; j++) {
                                    String columnName = columns[p][j];
                                    if (columnName.equals(nodeString)) {
                                        selectedImageIcon = ValueHolder.Icons.PENCIL;
                                        break l;
                                    } else if (j == columns[p].length - 1) {
                                        for (int q = 0; q < tables.length; q++) {
                                            for (int r = 0; r < columns[q].length; r++) {
                                                for (int k = 0; k < columnInfos[q][r].length; k++) {
                                                    String columnInfo = columnInfos[q][r][k];
                                                    if (columnInfo.equals(nodeString)) {
                                                        switch (k) {
                                                            case 0: {
                                                                selectedImageIcon = ValueHolder.Icons.HASH_TAG;
                                                                break l;
                                                            }
                                                            case 1: {
                                                                selectedImageIcon = ValueHolder.Icons.BLUE;
                                                                break l;
                                                            }
                                                            case 2: {
                                                                selectedImageIcon = ValueHolder.Icons.ORANGE;
                                                                break l;
                                                            }
                                                            case 3: {
                                                                selectedImageIcon = ValueHolder.Icons.GREEN;
                                                                break l;
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                setIcon(selectedImageIcon);
                return this;
            }

        });

        tableInfoTree.addTreeSelectionListener(new TreeSelectionListener() {

            @Override
            public void valueChanged(TreeSelectionEvent evt) {
                Object[] data = evt.getPath().getPath();
                selectedTreeNodeValue = String.valueOf(data[data.length - 1]);
            }
        });

        tableInfoTree.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent evt) {
                if (evt.getButton() == MouseEvent.BUTTON3) {
                    if (selectedTreeNodeValue.equals(databaseName)) {
                        showTreeNodePopup(evt.getX(), evt.getY(), true);
                    } else {
                        for (String table : tables) {
                            if (table.equals(selectedTreeNodeValue)) {
                                showTreeNodePopup(evt.getX(), evt.getY(), false);
                                break;
                            }
                        }
                    }
                } else if (evt.getClickCount() >= 2) {
                    queryingTextArea.setText(queryingTextArea.getText() + "`" + selectedTreeNodeValue + "`");
                }
            }

        });

        currentHistoryList = historyManager.getHistoryList();
        for (int i = 0; i < currentHistoryList.size(); i++) {
            String[] history = currentHistoryList.get(i);
            historyContainingPanel.add(new HistoryItemPanel(i + 1, history[0].equals(applicationID), history[1], queryingTextArea));
        }
        adjustHistoryScrollbar();
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    MainFrame.this.historyManager.saveApplicationDb();
                } catch (HistoryManagerException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage(), "Application history saving", JOptionPane.ERROR_MESSAGE);
                }
            }
        }));

    }

    private void refreshDatabase() {
        inputQuery = "";
        invalidateInputOutput();
        new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground() throws Exception {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                }
                try {
                    String tableNames = MainFrame.this.ioManager.getTableNames();
                    tableNameJSONArray = new JSONArray(tableNames);
                } catch (IOManagerException ex) {
                    showDeviceDisconnectedDialog();
                }
                return null;
            }

            @Override
            protected void done() {
                closeProgressDialog();
            }

        }.execute();
        showProgressDialog(true, 0, "Waiting for table list");

        tableInfoTree.removeAll();
        tableInfoTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        final DefaultMutableTreeNode databaseDMTN = new DefaultMutableTreeNode(databaseName);
        tableInfoTree.setModel(new DefaultTreeModel(databaseDMTN));
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                }
                try {
                    tables = new String[tableNameJSONArray.length()];
                    queries = new String[tableNameJSONArray.length()];
                    columns = new String[tableNameJSONArray.length()][];
                    columnInfos = new String[tableNameJSONArray.length()][][];
                    for (int i = 0; i < tableNameJSONArray.length(); i++) {
                        tables[i] = tableNameJSONArray.getJSONObject(i).getString("name");
                        DefaultMutableTreeNode tableDMTN = new DefaultMutableTreeNode(tables[i]);
                        databaseDMTN.add(tableDMTN);
                        queries[i] = tableNameJSONArray.getJSONObject(i).getString("sql");
                        String tableColumnInfo = MainFrame.this.ioManager.getTableColumnInfo(tables[i]);
                        JSONArray columnJSONArray = new JSONArray(tableColumnInfo);
                        columns[i] = new String[columnJSONArray.length()];
                        columnInfos[i] = new String[columnJSONArray.length()][];
                        for (int j = 0; j < columnJSONArray.length(); j++) {
                            JSONObject columnJSONObject = columnJSONArray.getJSONObject(j);
                            columns[i][j] = columnJSONObject.getString("name");
                            columnInfos[i][j] = new String[4];
                            DefaultMutableTreeNode columnDMTN = new DefaultMutableTreeNode(columns[i][j]);
                            columnInfos[i][j][0] = "Column ID : " + String.valueOf(columnJSONObject.getInt("cid"));
                            columnDMTN.add(new DefaultMutableTreeNode(columnInfos[i][j][0]));
                            columnInfos[i][j][1] = "Type : " + columnJSONObject.getString("type");
                            columnDMTN.add(new DefaultMutableTreeNode(columnInfos[i][j][1]));
                            columnInfos[i][j][2] = "Def value : " + String.valueOf(columnJSONObject.getString("dflt_value"));
                            columnDMTN.add(new DefaultMutableTreeNode(columnInfos[i][j][2]));
                            columnInfos[i][j][3] = "Not NULL : " + String.valueOf((columnJSONObject.getInt("notnull") == 1));
                            columnDMTN.add(new DefaultMutableTreeNode(columnInfos[i][j][3]));
                            tableDMTN.add(columnDMTN);
                        }
                        databaseDMTN.add(tableDMTN);
                        waitingDialog.incrementProgressBar();
                    }
                } catch (IOManagerException ex) {
                    showDeviceDisconnectedDialog();
                }
                return null;
            }

            @Override
            protected void done() {
                closeProgressDialog();
            }

        }.execute();
        showProgressDialog(false, tableNameJSONArray.length(), "Getting info of " + tableNameJSONArray.length() + " tables");
        tableInfoTree.expandPath(new TreePath((TreeNode) tableInfoTree.getModel().getRoot()));
        tableNameJSONArray = null;

    }

    private void showProgressDialog(boolean interminidate, int maxProgressValue, String message) {
        waitingDialog = new WaitingDialog(MainFrame.this, interminidate, maxProgressValue, message);
        waitingDialog.setVisible(true);
    }

    private void closeProgressDialog() {
        waitingDialog.setVisible(false);
        waitingDialog.dispose();
    }

    private void showDeviceDisconnectedDialog() {
        JOptionPane.showMessageDialog(null, "Device " + deviceName + " is beign disconnected", "Connectio error", JOptionPane.ERROR_MESSAGE);
        System.exit(0);
        System.gc();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton2 = new javax.swing.JButton();
        splitPane0 = new javax.swing.JSplitPane();
        queryHistoryContainerPanel = new javax.swing.JPanel();
        jTabbedPane4 = new javax.swing.JTabbedPane();
        jScrollPane3 = new javax.swing.JScrollPane();
        tableInfoTree = new javax.swing.JTree();
        jPanel4 = new javax.swing.JPanel();
        splitPane1 = new javax.swing.JSplitPane();
        splitPane2 = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        queryRootConatinerPanel = new javax.swing.JPanel();
        jTabbedPane3 = new javax.swing.JTabbedPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        queryingTextArea = new javax.swing.JTextArea();
        tableContainerPanel = new javax.swing.JPanel();
        jTabbedPane2 = new javax.swing.JTabbedPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        resultTable = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        historyScrollPane = new javax.swing.JScrollPane();
        jPanel5 = new javax.swing.JPanel();
        historyContainingPanel = new javax.swing.JPanel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu3 = new javax.swing.JMenu();
        exitMenuItem = new javax.swing.JMenuItem();
        queryMenu = new javax.swing.JMenu();
        runQueryMenuItem = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jsonTextMenuItem = new javax.swing.JMenuItem();
        tableTextMenuItem = new javax.swing.JMenuItem();
        jMenu4 = new javax.swing.JMenu();
        refreshDatabaseMenuItem = new javax.swing.JMenuItem();

        jButton2.setText("jButton2");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        queryHistoryContainerPanel.setBackground(new java.awt.Color(254, 254, 254));

        jTabbedPane4.setOpaque(true);

        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("root");
        tableInfoTree.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        jScrollPane3.setViewportView(tableInfoTree);

        jTabbedPane4.addTab("Structure", jScrollPane3);

        javax.swing.GroupLayout queryHistoryContainerPanelLayout = new javax.swing.GroupLayout(queryHistoryContainerPanel);
        queryHistoryContainerPanel.setLayout(queryHistoryContainerPanelLayout);
        queryHistoryContainerPanelLayout.setHorizontalGroup(
            queryHistoryContainerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane4)
        );
        queryHistoryContainerPanelLayout.setVerticalGroup(
            queryHistoryContainerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 493, Short.MAX_VALUE)
        );

        splitPane0.setLeftComponent(queryHistoryContainerPanel);

        splitPane2.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jPanel1.setBackground(new java.awt.Color(254, 254, 254));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        splitPane2.setTopComponent(jPanel1);

        queryingTextArea.setColumns(20);
        queryingTextArea.setFont(new java.awt.Font("DialogInput", 0, 12)); // NOI18N
        queryingTextArea.setRows(5);
        jScrollPane1.setViewportView(queryingTextArea);

        jTabbedPane3.addTab("Query", jScrollPane1);

        javax.swing.GroupLayout queryRootConatinerPanelLayout = new javax.swing.GroupLayout(queryRootConatinerPanel);
        queryRootConatinerPanel.setLayout(queryRootConatinerPanelLayout);
        queryRootConatinerPanelLayout.setHorizontalGroup(
            queryRootConatinerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 292, Short.MAX_VALUE)
        );
        queryRootConatinerPanelLayout.setVerticalGroup(
            queryRootConatinerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 137, Short.MAX_VALUE)
        );

        splitPane2.setTopComponent(queryRootConatinerPanel);

        resultTable.setAutoCreateRowSorter(true);
        resultTable.setFont(new java.awt.Font("Consolas", 0, 11)); // NOI18N
        resultTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        resultTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        jScrollPane2.setViewportView(resultTable);

        jTabbedPane2.addTab("Result table", jScrollPane2);

        javax.swing.GroupLayout tableContainerPanelLayout = new javax.swing.GroupLayout(tableContainerPanel);
        tableContainerPanel.setLayout(tableContainerPanelLayout);
        tableContainerPanelLayout.setHorizontalGroup(
            tableContainerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 292, Short.MAX_VALUE)
        );
        tableContainerPanelLayout.setVerticalGroup(
            tableContainerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE)
        );

        splitPane2.setRightComponent(tableContainerPanel);

        splitPane1.setLeftComponent(splitPane2);

        historyContainingPanel.setAutoscrolls(true);

        javax.swing.GroupLayout historyContainingPanelLayout = new javax.swing.GroupLayout(historyContainingPanel);
        historyContainingPanel.setLayout(historyContainingPanelLayout);
        historyContainingPanelLayout.setHorizontalGroup(
            historyContainingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 415, Short.MAX_VALUE)
        );
        historyContainingPanelLayout.setVerticalGroup(
            historyContainingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 416, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(historyContainingPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(historyContainingPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 39, Short.MAX_VALUE))
        );

        historyScrollPane.setViewportView(jPanel5);

        jTabbedPane1.addTab("History", historyScrollPane);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1)
        );

        splitPane1.setRightComponent(jPanel2);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(splitPane1, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(splitPane1)
        );

        splitPane0.setRightComponent(jPanel4);

        jMenu3.setText("File");

        exitMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.ALT_MASK));
        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        jMenu3.add(exitMenuItem);

        jMenuBar1.add(jMenu3);

        queryMenu.setText("Query");

        runQueryMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F12, java.awt.event.InputEvent.SHIFT_MASK));
        runQueryMenuItem.setText("Run");
        runQueryMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runQueryMenuItemActionPerformed(evt);
            }
        });
        queryMenu.add(runQueryMenuItem);

        jMenuBar1.add(queryMenu);

        jMenu2.setText("Result");

        jsonTextMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_J, java.awt.event.InputEvent.CTRL_MASK));
        jsonTextMenuItem.setText("JSON text view");
        jsonTextMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jsonTextMenuItemActionPerformed(evt);
            }
        });
        jMenu2.add(jsonTextMenuItem);

        tableTextMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_T, java.awt.event.InputEvent.CTRL_MASK));
        tableTextMenuItem.setText("Table text view");
        tableTextMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tableTextMenuItemActionPerformed(evt);
            }
        });
        jMenu2.add(tableTextMenuItem);

        jMenuBar1.add(jMenu2);

        jMenu4.setText("Database");

        refreshDatabaseMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.SHIFT_MASK));
        refreshDatabaseMenuItem.setText("Refresh");
        refreshDatabaseMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshDatabaseMenuItemActionPerformed(evt);
            }
        });
        jMenu4.add(refreshDatabaseMenuItem);

        jMenuBar1.add(jMenu4);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(splitPane0)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(splitPane0)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void runQueryMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runQueryMenuItemActionPerformed
        // TODO add your handling code here:
        inputQuery = queryingTextArea.getText();
        if (inputQuery != null && !inputQuery.isEmpty()) {
            historyManager.addApplicationHistory(applicationID, inputQuery);
            historyContainingPanel.add(new HistoryItemPanel(currentHistoryList.size(), true, inputQuery, queryingTextArea));
            historyContainingPanel.updateUI();
            adjustHistoryScrollbar();
            runQuery();
        } else {
            JOptionPane.showMessageDialog(null, "Query field is empty", "Query execution", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_runQueryMenuItemActionPerformed

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        // TODO add your handling code here:
        System.exit(0);
    }//GEN-LAST:event_exitMenuItemActionPerformed

    private void refreshDatabaseMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshDatabaseMenuItemActionPerformed
        // TODO add your handling code here:
        refreshDatabase();
    }//GEN-LAST:event_refreshDatabaseMenuItemActionPerformed

    private void jsonTextMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jsonTextMenuItemActionPerformed
        if (outputResult != null && !outputResult.isEmpty()) {
            try {
                // TODO add your handling code here:
                JSONArray jsonArray = new JSONArray(outputResult);
                if (jsonArray.length() > 0) {
                    String formattedResult = jsonArray.toString(2);
                    new TextOutputDialog(this, formattedResult).setVisible(true);
                }
            } catch (JSONException ex) {
                JOptionPane.showMessageDialog(null, outputResult, "Result error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_jsonTextMenuItemActionPerformed

    private void tableTextMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tableTextMenuItemActionPerformed
        // TODO add your handling code here:        
        if (outputResultAsTextTable != null) {
            boolean errorOccured = false;
            if (outputResultAsTextTable.isEmpty()) {
                errorOccured = processResult(true);
            }
            if (!errorOccured) {
                new TextOutputDialog(this, outputResultAsTextTable).setVisible(true);
            }
        }
    }//GEN-LAST:event_tableTextMenuItemActionPerformed

    public void setTableQueryList(Map<String, String> tableQueryList) {
        this.tableQueryList = tableQueryList;
    }

    public void setTableChangeList(List<Row> rowsToChange, List<Row> rowsToInsert, List<Row> rowsToUpdate, List<Row> rowsToDelete) {
        this.rowsToChange = rowsToChange;
        this.rowsToInsert = rowsToInsert;
        this.rowsToUpdate = rowsToUpdate;
        this.rowsToDelete = rowsToDelete;
    }

    private void showTreeNodePopup(int x, int y, boolean isDatabasePopup) {
        JPopupMenu treeNodePopup = new JPopupMenu();
        if (isDatabasePopup) {
            JMenuItem newTableMenuItem = new JMenuItem("New table");
            newTableMenuItem.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    System.out.println("new table");
                    new CreateTableDialog(MainFrame.this, true).setVisible(true);
                    if (tableQueryList != null && tableQueryList.size() > 0) {
                        for (String query : tableQueryList.values()) {
                            inputQuery = query;
                            runQuery();
                        }
                        refreshDatabase();
                        tableQueryList = null;
                    }
                }
            });
            treeNodePopup.add(newTableMenuItem);
        } else {
            JMenuItem viewAllMenuItem = new JMenuItem("View table");
            viewAllMenuItem.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    System.out.println("view all");
                    inputQuery = "select * from `" + selectedTreeNodeValue + "`";
                    queryingTextArea.setText(inputQuery);
                    runQuery();
                }
            });
            treeNodePopup.add(viewAllMenuItem);
            JMenuItem dropTableMenuItem = new JMenuItem("Drop table");
            dropTableMenuItem.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    System.out.println("drop table");
                    if (JOptionPane.showConfirmDialog(null, "All the data in table " + selectedTreeNodeValue + " will be lost!\nClick OK to delete the table", "Sqlite table dropping", JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
                        inputQuery = "drop table `" + selectedTreeNodeValue + "`";
                        queryingTextArea.setText(inputQuery);
                        runQuery();
                        refreshDatabase();
                    }
                }
            });
            treeNodePopup.add(dropTableMenuItem);
            JMenuItem updateTableMenuItem = new JMenuItem("Update table");
            updateTableMenuItem.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {

                    System.out.println("update table");

                    final Object[] columnsOfTable = getColumnsOfTable(selectedTreeNodeValue);

                    new SwingWorker<Void, Void>() {
                        String result;

                        @Override
                        protected Void doInBackground() throws Exception {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException ex) {
                            }
                            result = ioManager.executeQuery("select * from `" + selectedTreeNodeValue + "`");
                            return null;
                        }

                        @Override
                        protected void done() {
                            closeProgressDialog();
                            new UpdateTableDialog(MainFrame.this, true, result, selectedTreeNodeValue, columnsOfTable).setVisible(true);
                        }

                    }.execute();
                    showProgressDialog(true, 0, "Recieving data from table " + selectedTreeNodeValue);

                    if ((rowsToInsert != null && rowsToInsert.size() > 0) || (rowsToUpdate != null && rowsToUpdate.size() > 0) || (rowsToDelete != null && rowsToDelete.size() > 0)) {
                        new SwingWorker<Void, Void>() {

                            @Override
                            protected Void doInBackground() {
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException ex) {
                                }
                                String result = "";
                                try {
                                    for (Row row : rowsToInsert) {
                                        String insertQuery = createTableInsertQuery(selectedTreeNodeValue, row);
                                        System.out.println("insertQuery : " + insertQuery);
                                        result = ioManager.executeQuery(insertQuery);
                                        if (result.equals("[]")) {
                                            waitingDialog.incrementProgressBar();
                                        } else {
                                            throw new Exception();
                                        }
                                    }
                                    for (Row row : rowsToUpdate) {
                                        String updateQuery = createTableUpdateQuery(selectedTreeNodeValue, row);
                                        System.out.println("updateQuery : " + updateQuery);
                                        result = ioManager.executeQuery(updateQuery);
                                        if (result.equals("[]")) {
                                            waitingDialog.incrementProgressBar();
                                        } else {
                                            throw new Exception();
                                        }
                                    }
                                    for (Row row : rowsToDelete) {
                                        String deleteQuery = createTableDeleteQuery(selectedTreeNodeValue, row);
                                        System.out.println("deleteQuery : " + deleteQuery);
                                        result = ioManager.executeQuery(deleteQuery);
                                        if (result.equals("[]")) {
                                            waitingDialog.incrementProgressBar();
                                        } else {
                                            throw new Exception();
                                        }
                                    }
                                } catch (Exception ex) {
                                    JOptionPane.showMessageDialog(null, result, "Result error", JOptionPane.ERROR_MESSAGE);
                                }
                                return null;
                            }

                            @Override
                            protected void done() {
                                closeProgressDialog();
                                refreshDatabase();
                                rowsToInsert = null;
                                rowsToUpdate = null;
                                rowsToDelete = null;
                            }

                        }.execute();
                        showProgressDialog(false, rowsToInsert.size() + rowsToUpdate.size() + rowsToDelete.size(), "Applying changes to the dialog " + selectedTreeNodeValue);
                    }

                }
            });
            treeNodePopup.add(updateTableMenuItem);
            JMenuItem copyCreateStatementMenuItem = new JMenuItem("Copy create statement");
            copyCreateStatementMenuItem.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    System.out.println("copy create statement");
                    for (int i = 0; i < tables.length; i++) {
                        if (tables[i].equals(selectedTreeNodeValue)) {
                            StringSelection selection = new StringSelection(queries[i]);
                            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
                            JOptionPane.showMessageDialog(null, "Copied create statement of the table `" + selectedTreeNodeValue + "` to the clipboard", "Copy create statement", JOptionPane.INFORMATION_MESSAGE);
                            break;
                        }
                    }
                }
            });
            treeNodePopup.add(copyCreateStatementMenuItem);
            JMenuItem copyColumnNamesMenuItem = new JMenuItem("Copy column names");
            copyColumnNamesMenuItem.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    System.out.println("copy column names");
                    for (int i = 0; i < tables.length; i++) {
                        if (tables[i].equals(selectedTreeNodeValue)) {
                            String columnNames = "";
                            for (String column : columns[i]) {
                                columnNames = columnNames.concat(column + ",");
                            }
                            StringSelection selection = new StringSelection(columnNames.substring(0, columnNames.length() - 1));
                            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
                            JOptionPane.showMessageDialog(null, "Copied create statement of the table `" + selectedTreeNodeValue + "` to the clipboard", "Copy create statement", JOptionPane.INFORMATION_MESSAGE);
                            break;
                        }
                    }
                }
            });
            treeNodePopup.add(copyColumnNamesMenuItem);
        }

        treeNodePopup.show(tableInfoTree, x, y);
    }

    private void runQuery() {
        invalidateInputOutput();
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                }
                try {
                    outputResult = ioManager.executeQuery(inputQuery);
                    outputResultAsTextTable = "";
                } catch (IOManagerException ex) {
                    showDeviceDisconnectedDialog();
                }
                inputQuery = "";
                return null;
            }

            @Override
            protected void done() {
                closeProgressDialog();
                processResult(false);
            }

        }.execute();
        showProgressDialog(true, 0, "Receiving result set");

    }

    private synchronized boolean processResult(final boolean forTextOutput) {
        try {
            final JSONArray jsonArray = new JSONArray(outputResult);
            final int jsonObjectLength = jsonArray.length();

            if (jsonObjectLength > 0) {
                new SwingWorker<Void, Void>() {
                    
                    boolean columnsFound = false;
                    
                    List<String> tableColumnList = null;
                    List<Integer> tableColumnWidthList = null;
                    List<List<String>> tableRowList = null;
                    
                    @Override
                    protected Void doInBackground() throws Exception {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ex) {
                        }
                        
                        if (forTextOutput) {
                            tableColumnList = new ArrayList<String>();
                            tableColumnWidthList = new ArrayList<Integer>();
                            tableRowList = new ArrayList<List<String>>();
                        }
                        
                        for (int i = 0; i < jsonObjectLength; i++) {
                            waitingDialog.incrementProgressBar();
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            
                            if (!columnsFound) {
                                
                                if (forTextOutput) {
                                    tableColumnList.add("#");
                                    tableColumnWidthList.add(1);
                                } else {
                                    defaultTableModel.addColumn("#");
                                }
                                
                                JSONArray fieldNamesJSONArray = jsonObject.names();
                                for (int j = 0; j < fieldNamesJSONArray.length(); j++) {
                                    final String columnName = fieldNamesJSONArray.getString(j);
                                    if (forTextOutput) {
                                        tableColumnList.add(columnName);
                                        tableColumnWidthList.add(columnName.length());
                                    } else {
                                        defaultTableModel.addColumn(columnName);
                                    }
                                }
                                columnsFound = true;
                            }
                            
                            String rowIndex = String.valueOf(i);
                            
                            List<String> singleRowItemList = null;
                            Object[] rowData = null;
                            if (forTextOutput) {
                                singleRowItemList = new ArrayList<String>();
                                singleRowItemList.add(rowIndex);
                                if (tableColumnWidthList.get(0) < rowIndex.length()) {
                                    tableColumnWidthList.set(0, rowIndex.length());
                                }
                            } else {
                                rowData = new Object[defaultTableModel.getColumnCount()];
                                rowData[0] = rowIndex;
                            }
                            int columnLength = (forTextOutput ? tableColumnList.size() : resultTable.getColumnCount());
                            for (int j = 1; j < columnLength; j++) {
                                String columnValue = (forTextOutput ? tableColumnList.get(j) : resultTable.getColumnName(j));
                                String cellValue = String.valueOf(jsonObject.get(columnValue)).replaceAll("\n", "").replaceAll("\t", " ");
                                if (forTextOutput) {
                                    singleRowItemList.add(cellValue);
                                    if (tableColumnWidthList.get(j) < cellValue.length()) {
                                        tableColumnWidthList.set(j, cellValue.length());
                                    }
                                } else {
                                    rowData[j] = cellValue;
                                }
                            }
                            
                            if (forTextOutput) {
                                tableRowList.add(singleRowItemList);
                            } else {
                                defaultTableModel.addRow(rowData);
                            }
                            
                        }
                        
                        return null;
                    }
                    
                    @Override
                    protected void done() {
                        closeProgressDialog();
                        if (forTextOutput) {
                            if (tableColumnList.isEmpty() || tableRowList.isEmpty() || tableColumnWidthList.isEmpty()) {
                                JOptionPane.showMessageDialog(null, "No result found", "Tabular output", JOptionPane.WARNING_MESSAGE);
                            } else {
                                new SwingWorker<Void, Void>() {
                                    
                                    @Override
                                    protected Void doInBackground() throws Exception {
                                        try {
                                            Thread.sleep(100);
                                        } catch (InterruptedException ex) {
                                        }
                                        int boardWidth = 0;
                                        for (Integer tableColumnWidthList1 : tableColumnWidthList) {
                                            boardWidth += tableColumnWidthList1;
                                        }
                                        boardWidth += tableColumnList.size() + 1;
                                        Board board = new Board(boardWidth);
                                        outputResultAsTextTable = board.setInitialBlock(
                                                new Table(board, boardWidth, tableColumnList, tableRowList, tableColumnWidthList)
                                                .tableToBlocks()
                                        ).getPreview();
                                        return null;
                                    }
                                    
                                    @Override
                                    protected void done() {
                                        closeProgressDialog();
                                    }
                                    
                                }.execute();
                                showProgressDialog(true, 0, "Creating table for " + jsonObjectLength + " fields");
                            }
                        } else {
                            tableColumnAdjuster.adjustColumns();
                        }
                    }
                    
                }.execute();                
                showProgressDialog(false, jsonObjectLength, "Processing " + jsonObjectLength + " fields");
                return false;
            } else {
                return true;
            }
        } catch (JSONException ex) {
            JOptionPane.showMessageDialog(null, outputResult, "Result error", JOptionPane.ERROR_MESSAGE);
            return true;
        }
    }

    private void invalidateInputOutput() {        
        defaultTableModel.setRowCount(0);
        defaultTableModel.setColumnCount(0);
    }

    private String createTableInsertQuery(String tableName, Row row) {
        return "insert into `" + tableName + "` values(" + rowToCVSString(row) + ")";
    }

    private String createTableUpdateQuery(String tableName, Row row) {
        Row oldRow = rowsToChange.get(row.getRowIndex());
        Object[] columnsOfTable = getColumnsOfTable(tableName);
        return "update `" + tableName + "` set " + fieldValueCombinedString(columnsOfTable, row, " , ") + " where " + fieldValueCombinedString(columnsOfTable, oldRow, " and ");
    }

    private String createTableDeleteQuery(String tableName, Row row) {
        Object[] columnsOfTable = getColumnsOfTable(tableName);
        return "delete from `" + tableName + "` where " + fieldValueCombinedString(columnsOfTable, row, " and ");
    }

    private Object[] getColumnsOfTable(String tableName) {
        for (int i = 0; i < tables.length; i++) {
            if (tables[i].equals(tableName)) {
                return columns[i];
            }
        }
        return null;
    }

    private String fieldValueCombinedString(Object[] columns, Row row, String separator) {
        Object[] rowData = row.getRowData();
        String combination = "";
        for (int i = 0; i < rowData.length; i++) {
            combination = combination.concat(" `" + columns[i].toString() + "` = '" + rowData[i].toString().replaceAll("'", "''") + "' " + separator);
        }
        return combination.substring(0, combination.length() - separator.length());
    }

    private String rowToCVSString(Row row) {
        String cvsString = "";
        for (Object data : row.getRowData()) {
            cvsString = cvsString.concat("'" + data.toString().replaceAll("'", "''") + "' , ");
        }
        return cvsString.substring(0, cvsString.length() - 3);
    }

    private void adjustHistoryScrollbar() {
        historyScrollPane.getVerticalScrollBar().setValue(historyScrollPane.getVerticalScrollBar().getMaximum());
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JPanel historyContainingPanel;
    private javax.swing.JScrollPane historyScrollPane;
    private javax.swing.JButton jButton2;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTabbedPane jTabbedPane2;
    private javax.swing.JTabbedPane jTabbedPane3;
    private javax.swing.JTabbedPane jTabbedPane4;
    private javax.swing.JMenuItem jsonTextMenuItem;
    private javax.swing.JPanel queryHistoryContainerPanel;
    private javax.swing.JMenu queryMenu;
    private javax.swing.JPanel queryRootConatinerPanel;
    private javax.swing.JTextArea queryingTextArea;
    private javax.swing.JMenuItem refreshDatabaseMenuItem;
    private javax.swing.JTable resultTable;
    private javax.swing.JMenuItem runQueryMenuItem;
    private javax.swing.JSplitPane splitPane0;
    private javax.swing.JSplitPane splitPane1;
    private javax.swing.JSplitPane splitPane2;
    private javax.swing.JPanel tableContainerPanel;
    private javax.swing.JTree tableInfoTree;
    private javax.swing.JMenuItem tableTextMenuItem;
    // End of variables declaration//GEN-END:variables
}

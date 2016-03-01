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
package com.clough.android.adbv.controller;

import com.clough.android.adbv.model.Row;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author thedathoudarya
 */
public class RowController {

    private static final RowController ROW_CONTROLLER = new RowController();

    private List<Row> rowList;

    private int initialRowCount;

    private RowController() {
    }

    public static RowController getPreparedRowController(List<Row> rowList) {
        ROW_CONTROLLER.rowList = rowList;
        ROW_CONTROLLER.initialRowCount = rowList.size();
        return ROW_CONTROLLER;
    }

    private void changeRowStatus(int rowIndex, int status) {
        Row row = rowList.get(rowIndex);
        if (row != null) {
            row.setRowStatus(status);
        }
    }

    public void insertRow(Object[] newRow) {
        rowList.add(new Row(rowList.size(), newRow, Row.INSERTED));
    }

    private int tableRowIndexToListIndex(int rowIndex) {
        return tableRowIndexToListIndex(0, rowIndex);
    }

    private int tableRowIndexToListIndex(int start, int rowIndex) {
        int skipCount = 0;
        for (int i = start; i <= rowIndex; i++) {
            Row row = rowList.get(i);
            if (row.getRowStatus() == Row.REMOVED) {
                skipCount++;
            }
        }
        if (skipCount == 0) {
            return rowIndex;
        } else {
            return tableRowIndexToListIndex(rowIndex + 1, rowIndex + skipCount);
        }
    }

    public void removeRow(int rowIndex) {
        changeRowStatus(tableRowIndexToListIndex(rowIndex), Row.REMOVED);
    }

    public void updateRow(int rowIndex, Object[] updatedRow) {
        int listRowIndex = tableRowIndexToListIndex(rowIndex);
        rowList.set(listRowIndex, new Row(listRowIndex, updatedRow, Row.UPDATED));
    }

    public List<Row> getAllRows() {
        return rowList;
    }

    public List<Row> getRowsToInsert() {
        List<Row> insertedRowList = new ArrayList<Row>();
        for (int i = initialRowCount; i < rowList.size(); i++) {
            Row row = rowList.get(i);
            if (row.getRowStatus() == Row.INSERTED || row.getRowStatus() == Row.UPDATED) {
                insertedRowList.add(row);
            }
        }
        return insertedRowList;
    }

    public List<Row> getRowsToUpdate() {
        List<Row> updateRowList = new ArrayList<Row>();
        for (int i = 0; i < initialRowCount; i++) {
            Row row = rowList.get(i);
            if (row.getRowStatus() == Row.UPDATED) {
                updateRowList.add(row);
            }
        }
        return updateRowList;
    }

    public List<Row> getRowsToDelete() {
        List<Row> removedRowList = new ArrayList<Row>();
        for (int i = 0; i < initialRowCount; i++) {
            Row row = rowList.get(i);
            if (row.getRowStatus() == Row.REMOVED) {
                removedRowList.add(row);
            }
        }
        return removedRowList;
    }

}

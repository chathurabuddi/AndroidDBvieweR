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
package com.clough.android.adbv.model;

/**
 *
 * @author thedathoudarya
 */
public class Row {

    public static final int DEFAULT = 0;
    public static final int UPDATED = 1;
    public static final int INSERTED = 2;
    public static final int REMOVED = 3;
    
    private int rowIndex = 0;

    private Object[] rowData;

    private int rowStatus;

    public Row() {
    }

    public Row(int rowIndex, Object[] rowData, int rowStatus) {
        this.rowIndex = rowIndex;
        this.rowData = rowData;
        this.rowStatus = rowStatus;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }        

    public Object[] getRowData() {
        return rowData;
    }

    public void setRowData(Object[] rowData) {
        this.rowData = rowData;
    }

    public int getRowStatus() {
        return rowStatus;
    }

    public void setRowStatus(int rowStatus) {
        this.rowStatus = rowStatus;
    }
    
    

}

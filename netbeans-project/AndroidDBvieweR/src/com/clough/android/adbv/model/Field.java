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
 * @author ThedathOudarya
 */
public class Field {
    private int fieldIndex;
    private String fieldName;
    private String fieldType;
    private boolean fieldPrimarKey;
    private boolean fieldNotNull;
    private boolean fieldAutoIncrement;
    private boolean fieldUnique;
    private String fieldDefault;

    public Field() {
    }

    public Field(int fieldIndex, String fieldName, String fieldType, boolean fieldPrimarKey, boolean fieldNotNull, boolean fieldAutoIncrement, boolean fieldUnique, String fieldDefault) {
        this.fieldIndex = fieldIndex;
        this.fieldName = fieldName;
        this.fieldType = fieldType;
        this.fieldPrimarKey = fieldPrimarKey;
        this.fieldNotNull = fieldNotNull;
        this.fieldAutoIncrement = fieldAutoIncrement;
        this.fieldUnique = fieldUnique;
        this.fieldDefault = fieldDefault;
    }

    public String getFieldDefault() {
        return fieldDefault;
    }

    public void setFieldDefault(String fieldDefault) {
        this.fieldDefault = fieldDefault;
    }

    public int getFieldIndex() {
        return fieldIndex;
    }

    public void setFieldIndex(int fieldIndex) {
        this.fieldIndex = fieldIndex;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public boolean isFieldPrimarKey() {
        return fieldPrimarKey;
    }

    public void setFieldPrimarKey(boolean fieldPrimarKey) {
        this.fieldPrimarKey = fieldPrimarKey;
    }

    public boolean isFieldNotNull() {
        return fieldNotNull;
    }

    public void setFieldNotNull(boolean fieldNotNull) {
        this.fieldNotNull = fieldNotNull;
    }

    public boolean isFieldAutoIncrement() {
        return fieldAutoIncrement;
    }

    public void setFieldAutoIncrement(boolean fieldAutoIncrement) {
        this.fieldAutoIncrement = fieldAutoIncrement;
    }

    public boolean isFieldUnique() {
        return fieldUnique;
    }

    public void setFieldUnique(boolean fieldUnique) {
        this.fieldUnique = fieldUnique;
    }        
    
}

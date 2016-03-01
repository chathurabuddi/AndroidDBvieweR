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
package com.clough.android.adbv.manager;

import com.clough.android.adbv.exception.HistoryManagerException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

/**
 *
 * @author thedathoudarya
 */
public class HistoryManager {

    private final File applicationDBFile;

//    private Map<String, ArrayList<String>> historyMap;
    
    private ArrayList<String[]> historyList;

    public HistoryManager(File applicationFile) throws HistoryManagerException {
        this.applicationDBFile = new File(applicationFile, "/application_db");
        if (!applicationDBFile.exists()) {
            saveApplicationDb(new ArrayList<String[]>());
        }
        this.historyList = readApplicationDb();
    }

    private void saveApplicationDb(ArrayList<String[]> historyList) throws HistoryManagerException {
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            fos = new FileOutputStream(applicationDBFile);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(historyList);
            oos.flush();
        } catch (FileNotFoundException ex) {
            throw new HistoryManagerException(ex);
        } catch (IOException ex) {
            throw new HistoryManagerException(ex);
        } finally {
            try {
                if (oos != null) {
                    oos.close();
                }
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException ex) {
                throw new HistoryManagerException(ex);
            }
        }
    }

    private ArrayList<String[]> readApplicationDb() throws HistoryManagerException {
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try {
            fis = new FileInputStream(applicationDBFile);
            ois = new ObjectInputStream(fis);
            return (ArrayList<String[]>) ois.readObject();
        } catch (FileNotFoundException ex) {
            throw new HistoryManagerException(ex);
        } catch (IOException ex) {
            throw new HistoryManagerException(ex);
        } catch (ClassNotFoundException ex) {
            throw new HistoryManagerException(ex);
        } finally {
            try {
                if (ois != null) {
                    ois.close();
                }
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException ex) {
                throw new HistoryManagerException(ex);
            }
        }
    }
    
    public ArrayList<String[]> getHistoryList() {
        return historyList;
    }

    public void addApplicationHistory(String appId, String query) {
        historyList.add(new String[]{appId, query});
    }      
    
    public void saveApplicationDb() throws HistoryManagerException {
        saveApplicationDb(historyList);
    }

}

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
package com.clough.android.adbv;

import com.clough.android.adbv.exception.ADBManagerException;
import com.clough.android.adbv.exception.HistoryManagerException;
import com.clough.android.adbv.manager.ADBManager;
import com.clough.android.adbv.manager.HistoryManager;
import com.clough.android.adbv.manager.IOManager;
import com.clough.android.adbv.view.MainFrame;
import com.clough.android.adbv.view.WaitingForDeviceDialog;
import com.jtattoo.plaf.aluminium.AluminiumLookAndFeel;
import java.io.IOException;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.metal.MetalLookAndFeel;

// Class responsible for the launch of the AndroidDBViewer desktop application
public class Launcher {
    
    public static void main(String[] args) {

        // Applying look and feel
        try {
            UIManager.setLookAndFeel(new AluminiumLookAndFeel());
        } catch (UnsupportedLookAndFeelException ex1) {
            try {
                UIManager.setLookAndFeel(new MetalLookAndFeel());
            } catch (UnsupportedLookAndFeelException ex) {
            }
        }
        
        // Displaying a dialog until connect with a android application
        WaitingForDeviceDialog waitingForDeviceDialog = new WaitingForDeviceDialog();
        waitingForDeviceDialog.setVisible(true);
                
        String errorMessage = "";
        try {
            
            // Preparing for the oparations based on android debugger bridge(adb)
            ADBManager adbManager = new ADBManager();
            
            // Starts configuring the desktop application HistoryManager
            HistoryManager historyManager = new HistoryManager(adbManager.getApplicationFile());
            
            // Connecting with the android device and one of it's installed application
            // which use ADBVApplication as it's 'application'
            IOManager ioManager = adbManager.makeConnection();            
            waitingForDeviceDialog.dispose();
            
            // Launching main view of the application
            new MainFrame(ioManager, historyManager).setVisible(true);
        } catch (ADBManagerException ex) {
            errorMessage = ex.getLocalizedMessage();
        } catch (IOException ex) {
            errorMessage = ex.getLocalizedMessage();
        } catch (HistoryManagerException ex) {
            errorMessage = ex.getLocalizedMessage();
        } finally {
            if (!errorMessage.isEmpty()) {
                JOptionPane.showMessageDialog(null, errorMessage, "Application error", JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }
        }
    }

}

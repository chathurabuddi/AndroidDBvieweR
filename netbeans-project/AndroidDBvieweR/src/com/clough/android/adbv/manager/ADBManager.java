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

import com.clough.android.adbv.Launcher;
import com.clough.android.adbv.exception.ADBManagerException;
import com.clough.android.adbv.model.Data;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import javax.swing.JOptionPane;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * Manage operations based on android debugger bridge(adb)
 */
public final class ADBManager {

    // Port number that desktop application sockets use
    public static final int PC_PORT = 1992;
    
    // Port number that server socket in android application uses
    public static final int APP_PORT = 1993;
    
    // Operation system
    private final String OS;
    
    // Adb, executable file path
    private String adbPath;
    
    // Application's main file path
    private final File APPLICATION_FILE;
    
    // System home path
    private final String SYSTEM_HOME_PATH;

    public ADBManager() throws ADBManagerException {
        
        // Getting the operation system name
        OS = System.getProperty("os.name").toLowerCase();
        
        // Getting the path of the home directory of this system
        SYSTEM_HOME_PATH = System.getProperty("user.home");
        
        if (!new File(SYSTEM_HOME_PATH).canWrite()) {
            
            // This application uses system default home directory to store the 
            // adb executable file and application_db file.
            // If the system home diretory isn't writable, application cannot proceed further
            throw new ADBManagerException("User home directory is not writable");
        }
        
        // Creating a new directory named 'adbv' for application's purposes
        APPLICATION_FILE = new File(SYSTEM_HOME_PATH, "/.android/adbv/");
        if (!APPLICATION_FILE.exists() && !APPLICATION_FILE.mkdirs()) {
            
            // If cannot create the directory, application cannot proceed furhter
            throw new ADBManagerException("Application direcory not created");
        }
        
        // Make the 'adbv' directory writable, if currently it's not
        if (!APPLICATION_FILE.canWrite()) {
            APPLICATION_FILE.setWritable(true);
        }
    }

    /**
     * Selecting the relevant executable adb file/s for copying into the application's directory
     * @throws ADBManagerException
     * @throws IOException 
     */
    private void placeRelevantAdb() throws ADBManagerException, IOException {
        if (OS.contains("windows")) {
            String windowsPath = "/adb/windows/";
            adbPath = copyAssets(windowsPath, "adb.exe");
            copyAssets(windowsPath, "AdbWinApi.dll");
            copyAssets(windowsPath, "AdbWinUsbApi.dll");
        } else if (OS.contains("linux")) {
            String linuxPath = "/adb/linux/";
            adbPath = copyAssets(linuxPath, "adb");
        } else if (OS.contains("mac")) {
            String macPath = "/adb/mac/";
            adbPath = copyAssets(macPath, "adb");
        } else {
            // Only windows, ubuntu and mac OSes can run AndroidDBviewer
            throw new ADBManagerException("Unsupproted operation system.");
        }
    }

    /**
     * Copying the file by the given file name from the source directory 
     * to application's directory
     * @param sourceFilePath Source file path depending on the OS
     * @param fileName Name of the file in the sourceFilePath to be copied
     * @return Absolute file path to the copied file
     * @throws IOException
     * @throws ADBManagerException 
     */
    private String copyAssets(String sourceFilePath, String fileName) throws IOException, ADBManagerException {
        File out = new File(APPLICATION_FILE, fileName);
        if (!out.exists()) {
            InputStream fis = Launcher.class.getResourceAsStream(sourceFilePath + fileName);
            if (fis != null) {
                FileOutputStream fos = new FileOutputStream(out);
                int read;
                while ((read = fis.read()) != -1) {
                    fos.write(read);
                }
                fos.flush();
                fis.close();
                fos.close();
            }
        }
        if (!out.setExecutable(true)) {
            throw new ADBManagerException("Copied ADB file couldn't make executable");
        }
        return out.getAbsolutePath();

    }

    /**
     * Executing the adb commands given in var-args
     * @param args adb command in var-args
     */
    private synchronized void adb(final String... args) {
        String[] commands = new String[args.length + 1];
        commands[0] = adbPath;
        System.arraycopy(args, 0, commands, 1, args.length);
        try {
            Runtime.getRuntime().exec(commands).waitFor();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, ex.getLocalizedMessage(), "Android DB Viewer", JOptionPane.ERROR_MESSAGE);
        } catch (InterruptedException ex) {
            JOptionPane.showMessageDialog(null, ex.getLocalizedMessage(), "Android DB Viewer", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Starting android debugger bridge
     */
    private void startADB() {
        adb("start-server");
    }
    
    /**
     * Ending the android debugger bridge
     */
    private void killServer() {
        adb("kill-server");
    }

    /**
     * Forwarding the PC_PORT which sockets of desktop application uses, to the APP_PORT
     * which server socket of android application uses.
     */
    private void portForwarding() {
        adb("forward", "tcp:" + PC_PORT, "tcp:" + APP_PORT);
    }

    /**
     * Executing relevant adb commands in the right order to make the connection 
     * with the device and then with the ADBVApplication configured app.
     */
    private void prepareADB() {
        try {
            startADB();
            portForwarding();
            Thread.sleep(1500);
        } catch (InterruptedException ex1) {
        }
    }

    /**
     * Waiting for a device to connect and connect with it's one of eligible 
     * android application for AndroidDBViewer to run.
     * @return IOManager instance configured for a android application
     * @throws IOException
     * @throws ADBManagerException 
     */
    public IOManager makeConnection() throws IOException, ADBManagerException {
        
        // Placing adb executable files for the desktop application use
        placeRelevantAdb();

        // Trying to connect with android application again and agian 
        // when ever an attempt is fail
        while (true) {            
            try {
                
                // Creating a socket with PC_PORT and trying to connect with an server socket.
                // Connecting can fails due the ports not being forwarded or due to the
                // server socket not being started yet.
                // To be able to detect the server socket as running, a device must connected to the PC
                // and ADBVApplication configured android app must run on the device.
                final Socket deviceSocket = new Socket("localhost", PC_PORT);
                
                // Requesting to connect
                new PrintWriter(deviceSocket.getOutputStream(), true).println(new Data(Data.CONNECTION_REQUEST, "", "").toJSON().toString());                
                Data recivedData =  new Data(new JSONObject(new BufferedReader(new InputStreamReader(deviceSocket.getInputStream())).readLine()));
                if (recivedData.getStatus() == Data.CONNECTION_ACCEPTED) {
                    
                    // Adding a shudown hook to disconnect the adb connection and close the socket connection
                    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

                        @Override
                        public void run() {
                            killServer();
                            try {
                                deviceSocket.shutdownInput();
                                deviceSocket.shutdownOutput();
                                deviceSocket.close();
                            } catch (IOException ex) {
                            }
                        }
                    }));
                    
                    // Returning an IOManager created for this connection
                    return new IOManager(deviceSocket);
                }
            } catch (IOException ex) {
                prepareADB();
            } catch (NullPointerException ex) {
                prepareADB();
            } catch (JSONException ex) {
                prepareADB();
            }
        }
    }

    public File getApplicationFile() {
        return APPLICATION_FILE;
    }

}

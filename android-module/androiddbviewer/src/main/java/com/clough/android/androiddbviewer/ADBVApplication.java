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

package com.clough.android.androiddbviewer;

import android.app.Application;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Build;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Custom Application class for enabling AndroidDBViewer application to be detected
 * in order to monitor and manage the database of the application.
 */
public abstract class ADBVApplication extends Application {

    private boolean flag = true;

    private ServerSocket serverSocket;

    private Socket socket;

    private BufferedReader br;

    private PrintWriter pw;

    private SQLiteOpenHelper sqliteOpenHelper;

    /**
     * Called by the ADBVApplication to get the user defined(custom) SQLiteOpenHelper instance.
     * All the database operations of AndroidDBViewer will based upon this SQLiteOpenHelper.
     *
     * @return User defined SQLiteOpenHelper instance
     */
    public abstract SQLiteOpenHelper getDataBase();

    @Override
    public void onCreate() {
        super.onCreate();

        // Getting user configured(custom) SQLiteOpenHelper instance.
        sqliteOpenHelper = getDataBase();

        // getDataBase() could return a null
        if (sqliteOpenHelper != null) {

            // Background operation of creating the server socket.
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {

                        // Server socket re create when device is being disconnected or
                        // when AndroidDBViewer desktop application is being closed.
                        // Creating server socket will exit when
                        // android application runs in low memory or when
                        // android application being terminated due some reasons.
                        l1:
                        while (flag) {
                            serverSocket = new ServerSocket(1993);
                            socket = serverSocket.accept();
                            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            pw = new PrintWriter(socket.getOutputStream(), true);

                            // Keeps a continuous communication between android application and
                            // AndroidDBViewer desktop application through IO streams of the accepted socket connection.
                            // There will be continuous data parsing between desktop application and android application.
                            // Identification of device being disconnected or desktop application being closed will be determined
                            // only when there is a NULL data being received.
                            l2:
                            while (flag) {

                                // Format of the parsing data string is JSON, a content of a 'Data' instance
                                String requestJSONString = br.readLine();

                                if (requestJSONString == null) {

                                    // Received a null response from desktop application, due to disconnecting the
                                    // device or closing the AndroidDBViewer desktop application.
                                    // Therefore, closing all current connections and streams to re create the server
                                    // socket so that desktop application can connect in it's next run.

                                    // Device disconnection doesn't produce an IOException.
                                    // Also, even after calling
                                    // socket.close(), socket.shutdownInput() and socket.shutdownOutput()
                                    // within a shutdown hook in desktop application, the socket connection
                                    // in this async task always gives
                                    // socket.isConnected() as 'true' ,
                                    // socket.isClosed() as 'false' ,
                                    // socket.isInputShutdown() as 'false' and
                                    // socket.isOutputShutdown() as 'false' .
                                    // But, bufferedReader.readLine() starts returning 'null' continuously.
                                    // So, inorder to desktop application to connect with the device again,
                                    // there should be a ServerSocket waiting to accept a socket connection, in device.
                                    closeConnection();
                                    continue l1;
                                } else {

                                    // Received a valid response from the desktop application.
                                    Data data;
                                    try {

                                        // Converting received request to a 'Data' instance.
                                        data = new Data(new JSONObject(requestJSONString));
                                        int status = data.getStatus();
                                        if (status == Data.CONNECTION_REQUEST) {

                                            // Very first request from desktop application to
                                            // establish the connection and setting the response as
                                            // connection being accepted.
                                            data.setStatus(Data.CONNECTION_ACCEPTED);
                                        } else if (status == Data.LIVE_CONNECTION) {

                                            // When there is no user interaction in desktop application,
                                            // data being passed from desktop application to android
                                            // application with the status of LIVE_CONNECTION, and the
                                            // same data send again to the desktop application from android application,
                                            // to notify that connection is still alive.
                                            // This exchange won't change until  there is a request from
                                            // desktop application with a different status.
                                        } else if (status == Data.QUERY) {

                                            // Requesting to perform a query execution.

                                            String result = "No result";
                                            try {

                                                // Performing select, insert, delete and update queries.
                                                Cursor cursor = sqliteOpenHelper.getWritableDatabase().rawQuery(data.getQuery(), null);

                                                // Flag to identify the firs move of the cursor
                                                boolean firstTime = true;

                                                int columnCount = 0;

                                                // JSONArray to hold the all JSONObjects, created per every row
                                                // of the result returned, executing the given query.
                                                JSONArray jsonArray = new JSONArray();

                                                // Moving the cursor to the next row of retrieved result
                                                // after executing the requested query.
                                                while (cursor.moveToNext()) {

                                                    if (firstTime) {

                                                        // Column count of the result returned, executing the given query.
                                                        columnCount = cursor.getColumnCount();
                                                        firstTime = false;
                                                    }

                                                    // JOSNObject to hold the values of a single row
                                                    JSONObject jsonObject = new JSONObject();
                                                    for (int i = 0; i < columnCount; i++) {
                                                        int columnType = cursor.getType(i);
                                                        String columnName = cursor.getColumnName(i);
                                                        if (columnType == Cursor.FIELD_TYPE_STRING) {
                                                            jsonObject.put(columnName, cursor.getString(i));
                                                        } else if (columnType == Cursor.FIELD_TYPE_BLOB) {
                                                            jsonObject.put(columnName, cursor.getBlob(i).toString());
                                                        } else if (columnType == Cursor.FIELD_TYPE_FLOAT) {
                                                            jsonObject.put(columnName, String.valueOf(cursor.getFloat(i)));
                                                        } else if (columnType == Cursor.FIELD_TYPE_INTEGER) {
                                                            jsonObject.put(columnName, String.valueOf(cursor.getInt(i)));
                                                        } else if (columnType == Cursor.FIELD_TYPE_NULL) {
                                                            jsonObject.put(columnName, "NULL");
                                                        } else {
                                                            jsonObject.put(columnName, "invalid type");
                                                        }
                                                    }
                                                    jsonArray.put(jsonObject);
                                                }
                                                result = jsonArray.toString();
                                                cursor.close();
                                            } catch (Exception e) {

                                                // If SQL error is occurred when executing the requested query,
                                                // error content will be the response to the desktop application.
                                                StringWriter sw = new StringWriter();
                                                PrintWriter epw = new PrintWriter(sw);
                                                e.printStackTrace(epw);
                                                result = sw.toString();
                                                epw.close();
                                                sw.close();
                                            } finally {
                                                data.setResult(result);
                                            }
                                        } else if (status == Data.DEVICE_NAME) {

                                            // Requesting device information
                                            data.setResult(Build.BRAND + " " + Build.MODEL);
                                        } else if (status == Data.APPLICATION_ID) {

                                            // Requesting application id (package name)
                                            data.setResult(getPackageName());
                                        } else if (status == Data.DATABASE_NAME) {

                                            // Requesting application database name.
                                            // Will provide the database name according
                                            // to the SQLiteOpenHelper user provided
                                            data.setResult(sqliteOpenHelper.getDatabaseName());
                                        } else {

                                            // Unidentified request state.
                                            closeConnection();
                                            continue l1;
                                        }
                                        String responseJSONString = data.toJSON().toString();
                                        pw.println(responseJSONString);
                                    } catch (JSONException e) {

                                        // Response couldn't convert to a 'Data' instance.
                                        // Desktop application will be notified to close the application.
                                        closeConnection();
                                        continue l1;
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        // Cannot create a server socket. Letting background process to end.
                    }
                }
            }).start();
        }
    }

    /**
     * Close all current connections and IO streams
     */
    private void closeConnection() {
        try {
            if (pw != null) {
                pw.close();
                pw = null;
            }
            if (br != null) {
                br.close();
                br = null;
            }
            if (socket != null) {
                socket.close();
                socket = null;
            }
            if (serverSocket != null) {
                serverSocket.close();
                serverSocket = null;
            }
        } catch (IOException e) {
            // already closed
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

        // Notifying background socket connection proses to stop re creating server socket and
        // managing communication between android application and desktop application.
        flag = false;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        // Notifying background socket connection proses to stop re creating server socket and
        // managing communication between android application and desktop application.
        flag = false;
    }
}

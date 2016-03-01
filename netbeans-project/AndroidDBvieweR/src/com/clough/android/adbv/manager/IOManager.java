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

import com.clough.android.adbv.exception.IOManagerException;
import com.clough.android.adbv.model.Data;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author thedathoudarya
 */
public class IOManager {

    private final Thread listenerThread;

    private final Socket adbSocket;

    private boolean stayAlive;

    private final PrintWriter pw;

    private final BufferedReader br;

    private final Data defaultData;

    private Data requestingData;

    private Data respondingData;

    private ConnectionLostListener connectionLostListener;

    public IOManager(Socket socket) throws IOException {
        this.adbSocket = socket;
        this.stayAlive = true;
        this.pw = new PrintWriter(adbSocket.getOutputStream(), true);
        this.br = new BufferedReader(new InputStreamReader(adbSocket.getInputStream()));
        this.defaultData = new Data(Data.LIVE_CONNECTION, "", "");
        this.requestingData = defaultData;

        listenerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (stayAlive) {
                    try {
                        pw.println(requestingData.toJSON().toString());
                        String receivedDataString = br.readLine();
                        respondingData = new Data(new JSONObject(receivedDataString));
                        Thread.sleep(10);
                    } catch (InterruptedException ex) {
                        stayAlive = false;
                        break;
                    } catch (IOException ex) {
                        stayAlive = false;
                        break;
                    } catch (JSONException ex) {
                        stayAlive = false;
                        break;
                    } catch (NullPointerException ex) {
                        stayAlive = false;
                        break;
                    } finally {
                        if (!stayAlive && connectionLostListener != null) {
                            connectionLostListener.onDisconnect();
                        }
                    }
                }
                try {
                    pw.close();
                    br.close();
                    adbSocket.close();
                } catch (IOException ex) {
                }
            }
        });
        listenerThread.start();
    }

    public void exit() {
        stayAlive = false;
    }

    private String waitForResult(Data requestingData) throws IOManagerException {
        this.requestingData = requestingData;
        String result = "";
        if (stayAlive) {
            while (respondingData == null || respondingData.getStatus() != requestingData.getStatus() || (requestingData.getStatus() == Data.QUERY && !requestingData.getQuery().equals(respondingData.getQuery()))) {
                this.requestingData = requestingData;
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                    throw new IOManagerException(ex.getLocalizedMessage());
                }
            }
            result = respondingData.getResult();
            this.requestingData = defaultData;
            return result;
        } else {
            throw new IOManagerException("Connection being disconnected");
        }
    }

    public String executeQuery(String query) throws IOManagerException {
        return waitForResult(new Data(Data.QUERY, query, ""));
    }

    public String getDeviceName() throws IOManagerException {
        return waitForResult(new Data(Data.DEVICE_NAME, "", ""));
    }

    public String getApplicationID() throws IOManagerException {
        return waitForResult(new Data(Data.APPLICATION_ID, "", ""));
    }
    
    public String getDatabaseName() throws IOManagerException {
        return waitForResult(new Data(Data.DATABASE_NAME, "", ""));
    }

    public void addConnectionLostListener(ConnectionLostListener connectionLostListener) {
        this.connectionLostListener = connectionLostListener;
    }

    public String getTableNames() throws IOManagerException {
        return executeQuery("select `name`, `sql` from sqlite_master where type = 'table' order by `name`");
    }

    public String getTableColumnInfo(String tableName) throws IOManagerException {
        return executeQuery("pragma table_info('" + tableName + "')");
    }   

    public interface ConnectionLostListener {
        public void onDisconnect();
    }

}

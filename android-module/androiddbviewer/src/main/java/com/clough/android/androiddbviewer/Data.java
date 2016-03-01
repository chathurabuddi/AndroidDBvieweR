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

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by thedathoudarya on 1/11/16.
 */
public class Data {

    public static final int LIVE_CONNECTION = 0;

    public static final int QUERY = 1;

    public static final int CONNECTION_REQUEST = 2;

    public static final int CONNECTION_ACCEPTED = 3;

    public static final int DEVICE_NAME = 4;

    public static final int APPLICATION_ID = 5;

    public static final int DATABASE_NAME = 6;

    private int status;

    private String query;

    private String result;

    public Data() {
    }

    public Data(JSONObject dataJSONObject) throws JSONException {
        parseData(dataJSONObject);
    }

    public Data(int status, String request, String response) {
        this.status = status;
        this.query = request;
        this.result = response;
    }

    public int getStatus() {
        return status;
    }

    public Data setStatus(int status) {
        this.status = status;
        return this;
    }

    public String getQuery() {
        return query;
    }

    public Data setQuery(String query) {
        this.query = query;
        return this;
    }

    public String getResult() {
        return result;
    }

    public Data setResult(String result) {
        this.result = result;
        return this;
    }

    public JSONObject toJSON() {
        try {
            return new JSONObject()
                    .put("status", status)
                    .put("query", query)
                    .put("result", result);
        } catch (JSONException ex) {
            return null;
        }
    }

    public Data parseData(JSONObject dataJSON) throws JSONException {
        this
                .setStatus(dataJSON.getInt("status"))
                .setQuery(dataJSON.getString("query"))
                .setResult(dataJSON.getString("result"));

        return this;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + this.status;
        hash = 53 * hash + (this.query != null ? this.query.hashCode() : 0);
        hash = 53 * hash + (this.result != null ? this.result.hashCode() : 0);
        return hash;

    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Data other = (Data) obj;
        if (this.status != other.status) {
            return false;
        }
        if ((this.query == null) ? (other.query != null) : !this.query.equals(other.query)) {
            return false;
        }
        if ((this.result == null) ? (other.result != null) : !this.result.equals(other.result)) {
            return false;
        }
        return true;
    }

}


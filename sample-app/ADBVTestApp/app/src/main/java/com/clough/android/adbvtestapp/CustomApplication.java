package com.clough.android.adbvtestapp;

import android.database.sqlite.SQLiteOpenHelper;

import com.clough.android.androiddbviewer.ADBVApplication;

/**
 * Created by Thedath Oudarya on 3/2/2016.
 */
public class CustomApplication extends ADBVApplication {
    @Override
    public SQLiteOpenHelper getDataBase() {
        return new DatabaseHelper(getApplicationContext());
    }
}

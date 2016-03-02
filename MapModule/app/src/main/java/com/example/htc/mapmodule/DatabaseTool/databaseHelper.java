package com.example.htc.mapmodule.DatabaseTool;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by HTC on 9/5/2015.
 */
public class databaseHelper extends SQLiteOpenHelper{
    private static final String DATABASE_NAME="databasemap";
    private static int database_version=1;
    public static final String TABLE_NAME="tablemap";
    public static final String ID_INDEX="name";
    public static final String ID_PLACE= "place_id";
    public String createdatabase="create table tablemap ( " +
            "name text, place_id text );";
    public databaseHelper(Context context)
    {
        super(context,DATABASE_NAME,null, database_version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(createdatabase);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}

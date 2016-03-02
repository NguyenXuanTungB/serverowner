package com.example.htc.mapmodule.DatabaseTool;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.sql.SQLException;

/**
 * Created by HTC on 9/6/2015.
 */
public class datasource {
    SQLiteDatabase sqLiteDatabase;
    databaseHelper databasehelper;
    public datasource(Context context)
    {
        databasehelper= new databaseHelper(context);
    }
    public void opendatabase()throws SQLException{
        sqLiteDatabase= databasehelper.getWritableDatabase();
    }
    public void closedatabase(){
        databasehelper.close();
    }
    public String getdata(String name){
        String result= null;
        Cursor cursor= sqLiteDatabase.query(databaseHelper.TABLE_NAME, null, null, null, null, null, null);
        for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext()){
            if(cursor.getString(0).equals(name)){
                result= cursor.getString(1);
                break;
            }
        }
        cursor.close();
        return result;
    }

}

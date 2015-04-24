package com.iverson.toby.rhealth;

/**
 * Created by Toby on 4/23/2015.
 */
import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MySQLiteHelper extends SQLiteOpenHelper {

    public static final String TABLE_Name = "inspections";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_Name = "name";
    public static final String COLUMN_Address = "address";
    public static final String COLUMN_Date = "date";
    public static final String COLUMN_RiskLevel = "risklevel";
    public static final String COLUMN_ViolationText = "violationtext";
    public static final String COLUMN_CodeViolation = "codeviolation";
    public static final String COLUMN_Critical = "critical";
    public static final String COLUMN_Rating = "rating";

    private static final String DATABASE_NAME = "RHealthIn.db";
    private static final int DATABASE_VERSION = 1;

    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_Name + "(" +
            COLUMN_ID + " integer primary key autoincrement, " +
            COLUMN_Name + " text not null, " +
            COLUMN_Address + " text not null, " +
            COLUMN_Date + " text, " +
            COLUMN_RiskLevel + " text, " +
            COLUMN_ViolationText + " text, " +
            COLUMN_CodeViolation + " text, " +
            COLUMN_Critical + " text, " +
            COLUMN_Rating + " test" +
            ");";

    public Context context;

    public MySQLiteHelper(Context c) {
        super(c, DATABASE_NAME, null, DATABASE_VERSION);
        context = c;
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);


        // puts data into database

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(MySQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_Name);
        onCreate(db);
    }

}
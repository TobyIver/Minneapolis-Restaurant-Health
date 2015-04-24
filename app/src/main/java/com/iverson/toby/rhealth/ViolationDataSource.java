package com.iverson.toby.rhealth;

/**
 * Created by Toby on 4/23/2015.
 */


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class ViolationDataSource {

    // Database fields
    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private String[] allColumns = { MySQLiteHelper.COLUMN_ID,
            MySQLiteHelper.COLUMN_Name, MySQLiteHelper.COLUMN_Address, MySQLiteHelper.COLUMN_Date,
            MySQLiteHelper.COLUMN_RiskLevel, MySQLiteHelper.COLUMN_ViolationText ,MySQLiteHelper.COLUMN_CodeViolation,
            MySQLiteHelper.COLUMN_Critical, MySQLiteHelper.COLUMN_Rating };

    public ViolationDataSource(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }
/*

    public Violation createComment(String comment) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_COMMENT, comment);
        long insertId = database.insert(MySQLiteHelper.TABLE_COMMENTS, null,
                values);
        Cursor cursor = database.query(MySQLiteHelper.TABLE_COMMENTS,
                allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        Violation newComment = cursorToComment(cursor);
        cursor.close();
        return newComment;
    }
*/
    public void initiate(Context context) {

        String mCSVfile = "Inspections.csv";
        AssetManager manager = context.getAssets();
        InputStream inStream = null;
        try {
            inStream = manager.open(mCSVfile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        BufferedReader buffer = new BufferedReader(new InputStreamReader(inStream));
        String line = "";
        database.beginTransaction();
        try {
            while ((line = buffer.readLine()) != null) {
                String[] colums = line.split(",");
                if (colums.length != 4) {
                    Log.d("CSVParser", "Skipping Bad CSV Row");
                    continue;
                }
                ContentValues cv = new ContentValues(3);
                cv.put(MySQLiteHelper.COLUMN_Name, colums[0].trim());
                cv.put(MySQLiteHelper.COLUMN_Address, colums[1].trim());
                cv.put(MySQLiteHelper.COLUMN_Date, colums[2].trim());
                cv.put(MySQLiteHelper.COLUMN_RiskLevel, colums[3].trim());
                cv.put(MySQLiteHelper.COLUMN_ViolationText, colums[4].trim());
                cv.put(MySQLiteHelper.COLUMN_CodeViolation, colums[4].trim());
                cv.put(MySQLiteHelper.COLUMN_Critical, colums[4].trim());
                cv.put(MySQLiteHelper.COLUMN_Rating, colums[4].trim());
                database.insert(MySQLiteHelper.TABLE_Name, null, cv);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        database.setTransactionSuccessful();
        database.endTransaction();

    }
}
/*
    public List<Comment> getAllComments() {
        List<Comment> comments = new ArrayList<Comment>();

        Cursor cursor = database.query(MySQLiteHelper.TABLE_COMMENTS,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Comment comment = cursorToComment(cursor);
            comments.add(comment);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return comments;
    }


    private Comment cursorToComment(Cursor cursor) {
        Comment comment = new Comment();
        comment.setId(cursor.getLong(0));
        comment.setComment(cursor.getString(1));
        return comment;
    }
    */


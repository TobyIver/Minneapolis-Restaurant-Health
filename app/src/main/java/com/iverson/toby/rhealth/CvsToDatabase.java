package com.iverson.toby.rhealth;

import android.database.sqlite.SQLiteDatabase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Toby on 4/23/2015.
 */
public class CvsToDatabase {
/*
    public boolean csvTodatabase() {
        FileStuff f = new FileStuff();
//check if extarnal drive is readerble
        if (!f.isExternalStorageReadable()) {
            f.fileError = "can not read external storage";
            f.fileinfo = "Please remount your SD card";
            return false;
        }
//get all files from extarnal drive data directory
        ArrayList<File> files = new ArrayList<File>();
        File directory = new File(FileStuff.DATA_DIRECTORY);
        if (!directory.exists()) {
            return false;
        }
        File[] fList = directory.listFiles();
        for (File file : fList) {
            if (file.isFile()) {
                files.add(file);
            }
        }
        for (File csvfile : files) {
            readFromFile(csvfile);
        }
        return true;
    }

    private void readFromFile(File file) {
        boolean gotColunms = false;
        String tableName = file.getName().replaceAll(".csv$", ""), sql, colunmNames="",colunmValues;
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            SQLiteDatabase database = this.getWritableDatabase();
            while ((line = br.readLine()) != null) {
                if (!gotColunms) {
//get colunm names
                    line = line.replaceAll("\"", "");
                    colunmNames = line;
                    gotColunms = true;//set flag to show we have colunms
                } else {
                    line = line.replaceAll("\"", "'");
                    colunmValues =  line;
//create sql query
                    sql = "INSERT INTO " + tableName + " ("+colunmNames+") VALUES ("+colunmValues+")";
//update datebase tables
                    database.execSQL(sql);
                }
            }
            database.close();
            br.close();
        } catch (IOException e) {
//You'll need to add proper error handling here
        }
    }
*/
}
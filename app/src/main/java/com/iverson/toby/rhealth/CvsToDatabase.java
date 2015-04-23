package com.iverson.toby.rhealth;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * Created by Toby on 4/23/2015.
 */
public class CvsToDatabase {


    FileReader file = new FileReader(Inspections.csv);
    BufferedReader buffer = new BufferedReader(file);
    String line = "";
    String tableName = "TABLE_NAME";
    String columns = "_id, name, dt1, dt2, dt3";
    String str1 = "INSERT INTO " + tableName + " (" + columns + ") values(";
    String str2 = ");";

    db.beginTransaction();
    while((line=buffer.readLine())!=null)

    {
        StringBuilder sb = new StringBuilder(str1);
        String[] str = line.split(",");
        sb.append("'" + str[0] + "',");
        sb.append(str[1] + "',");
        sb.append(str[2] + "',");
        sb.append(str[3] + "'");
        sb.append(str[4] + "'");
        sb.append(str2);
        db.execSQL(sb.toString());
    }

    db.setTransactionSuccessful();
    db.endTransaction();

}
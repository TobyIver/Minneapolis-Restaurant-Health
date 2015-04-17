package com.iverson.toby.rhealth;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

/**
 * Created by Toby on 4/10/2015.
 */
public class VsforP {
    private static final String DB_NAME = "databases/Health";

    //A good practice is to define database field names as constants
    private static final String TABLE_NAME = "friends";
    private static final String FRIEND_ID = "_id";
    private static final String FRIEND_NAME = "name";
    private static final String TAG = "VsforP";
    private static SQLiteDatabase database;
    private ArrayList friends;
    static Context c;




    public static ArrayList<Violation> get(Place p) {
        ArrayList<Violation> vs = null;
        ExternalDbOpenHelper dbOpenHelper = new ExternalDbOpenHelper(c, DB_NAME);
        database = dbOpenHelper.openDataBase();



        String name = p.getName();
        //name =name.toUpperCase();




        String address = p.getVicinity();
       // address = address.toUpperCase();
       // address = address.substring(0, address.indexOf(" "));



        return vs;
    }
}

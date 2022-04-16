package com.example.bloodpressuremonitor;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DBHandler extends SQLiteOpenHelper {

    // Constant variable for the database name.
    private static final String DB_NAME = "bloodpressuredb";

    // database version
    private static final int DB_VERSION = 1;

    // table name
    private static final String TABLE_NAME = "bloodpressuredata";

    // ID column -- probably date and time
    private static final String ID_COL = "id";

    // date-time column
    private static final String DATE_COL = "datetime";

    // Systolic BP column
    private static final String SYS_COL = "systolic";

    // Diastolic BP column
    private static final String DIA_COL = "diastolic";

    // creating a constructor for the database handler
    public DBHandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // below method: creating a database by running a query
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Making an SQLite Query and setting col names with data types
        String query = "CREATE TABLE " + TABLE_NAME + " ("
                + ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DATE_COL + " TEXT,"
                + SYS_COL + " TEXT,"
                + DIA_COL + " TEXT)";

        // Running the query to create the table.
        db.execSQL(query);
    }

    // TODO - Set up code to remove data from the table after a certain time period has passed.
    // I imagine this can be done with some fancy SQL queries that selects data from a certain time period and then just removing their entries.
    // This will require us to mess around with the settings.

    // adding new BP readings to the table:
    public void addBPData(String datetime, String systolic, String diastolic)
    {
        // getting the SQLite database and enabling write
        SQLiteDatabase db = this.getWritableDatabase();
        // creating content values
        ContentValues values = new ContentValues();
        // passing all values along with key-value pair
        values.put(DATE_COL, datetime);
        values.put(SYS_COL, systolic);
        values.put(DIA_COL, diastolic);
        // add values to the table
        db.insert(TABLE_NAME, null, values);
        // and then closing the database.
        db.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // called to check if the table exists already
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}

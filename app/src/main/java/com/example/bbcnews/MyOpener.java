package com.example.bbcnews;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyOpener extends SQLiteOpenHelper {

    protected final static String DATABASE_NAME = "BBCNewsDB";
    protected final static int VERSION_NUM = 1;
    public final static String TABLE_NAME = "FAVORITES";
    public final static String COL_TITLE = "TITLE";
    public final static String COL_LINK = "LINK";
    public final static String COL_ID = "_id";

    public MyOpener(Context context) {
        super(context, DATABASE_NAME, null, VERSION_NUM);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME
                + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_TITLE + " TEXT,"
                + COL_LINK + " TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL( "DROP TABLE IF EXISTS " + TABLE_NAME);

        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL( "DROP TABLE IF EXISTS " + TABLE_NAME);

        onCreate(db);
    }
}

package com.example.android.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

/**
 * Created by Ivars on 2017.04.19..
 */

public class InventoryDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "inventory.db";
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS" + InventoryEntry.TABLE_NAME;

    public InventoryDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //this will be called if the databese does not exist
        String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + InventoryEntry.TABLE_NAME + " (" +
                        InventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        InventoryEntry.COLUMN_INVENTORY_ITEM_NAME + " TEXT NOT NULL," +
                        InventoryEntry.COLUMN_INVENTORY_ITEM_QUANTITY + " INTEGER," +
                        InventoryEntry.COLUMN_INVENTORY_ITEM_PRICE + " REAL," +
                        InventoryEntry.COLUMN_INVENTORY_ITEM_SUPPLIER_MAIL + " TEXT," +
                        InventoryEntry.COLUMN_INVENTORY_ITEM_PICTURE + " BLOB" + ");";
        //execute the sqlite command for creating a table
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
}

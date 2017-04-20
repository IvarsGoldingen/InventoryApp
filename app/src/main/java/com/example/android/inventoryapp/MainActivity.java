package com.example.android.inventoryapp;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;
import com.example.android.inventoryapp.data.InventoryContract;
import com.example.android.inventoryapp.data.InventoryDbHelper;

import static android.R.attr.value;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.catalog_activity);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });
        displayDatabaseInfo();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_insert_dummy_data:
                insertDummyData();
                displayDatabaseInfo();
                //do something
                return true;
            case R.id.action_delete_all_entries:
                //do something
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        displayDatabaseInfo();
    }

    private void insertDummyData(){
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_INVENTORY_ITEM_NAME, "Telefons");
        values.put(InventoryEntry.COLUMN_INVENTORY_ITEM_QUANTITY, 5);
        values.put(InventoryEntry.COLUMN_INVENTORY_ITEM_PRICE, 2.7);
        values.put(InventoryEntry.COLUMN_INVENTORY_ITEM_SUPPLIER_MAIL, "telefoni@telefoni.com");
        //values.put(InventoryEntry.COLUMN_INVENTORY_ITEM_PICTURE, "");

        InventoryDbHelper mDbHelper = new InventoryDbHelper(this);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        long row = db.insert(InventoryEntry.TABLE_NAME, null, values);
    }

    private void displayDatabaseInfo() {
        // To access our database, we instantiate our subclass of SQLiteOpenHelper
        // and pass the context, which is the current activity.
        InventoryDbHelper mDbHelper = new InventoryDbHelper(this);

        // Create and/or open a database to read from it
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        //Projection represents the columns of the tab;e in which we are interested in
        //if set null we get all f them from the DB
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_INVENTORY_ITEM_NAME,
                InventoryEntry.COLUMN_INVENTORY_ITEM_PRICE,
                InventoryEntry.COLUMN_INVENTORY_ITEM_QUANTITY,
                InventoryEntry.COLUMN_INVENTORY_ITEM_SUPPLIER_MAIL,
        };
        //Selection allows to serach for a specifi value in the DB
        //like getting the 4th object by searching the ID
        //or getting all the items which have the name "pencil"
        String selection = InventoryEntry._ID + " = ?";
        //the ? is replaced by the selection args
        //this is used to protect from SQL injection
        String[] selectionArgs = { "1" };
        Cursor cursor = db.query(
                InventoryEntry.TABLE_NAME,                     // The table to query
                projection,                               // The columns to return
                null,                                // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                 // The sort order
        );
        try {
            // Display the number of rows in the Cursor (which reflects the number of rows in the
            // pets table in the database).
            TextView displayView = (TextView) findViewById(R.id.empty_title_text);
            displayView.setText("Number of rows in pets database table: " + cursor.getCount() + "\n\n");

            displayView.append(InventoryEntry._ID + " - " +
                    InventoryEntry._ID + " - " +
                    InventoryEntry.COLUMN_INVENTORY_ITEM_NAME + " - " +
                    InventoryEntry.COLUMN_INVENTORY_ITEM_PRICE + " - " +
                    InventoryEntry.COLUMN_INVENTORY_ITEM_QUANTITY + " - " +
                    InventoryEntry.COLUMN_INVENTORY_ITEM_SUPPLIER_MAIL + "\n");

            //to get a value from a column we must first get the column id
            int idColumnIndex = cursor.getColumnIndex(InventoryEntry._ID);
            int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_ITEM_NAME);
            int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_ITEM_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_ITEM_QUANTITY);
            int suplierEmailColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_ITEM_SUPPLIER_MAIL);
            //move through the cursor line by line
            //if the row is invalid(no more data in the cursor) false will be returned and we will
            //jumps out of the while loop
            //the cursor starts with the line -1, which has no data so we jump to the first row
            //before the first read
            while (cursor.moveToNext()){
                //getting data from the cursor
                int currentId = cursor.getInt(idColumnIndex);
                String currentName = cursor.getString(nameColumnIndex);
                long currentPrice = cursor.getLong(priceColumnIndex);
                int currentQuantity = cursor.getInt(quantityColumnIndex);
                String currentMail = cursor.getString(suplierEmailColumnIndex);

                displayView.append("\n" + currentId + " - " +
                        currentName + " - " +
                        currentPrice + " - " +
                        currentQuantity + " - " +
                        currentMail);
            }

        } finally {
            // Always close the cursor when you're done reading from it. This releases all its
            // resources and makes it invalid.
            cursor.close();
        }
    }
}

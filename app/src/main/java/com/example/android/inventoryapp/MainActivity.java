package com.example.android.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int INVENTORY_LOADER=0;

    InventoryItemAdapter itemAdapter;

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

        ListView inventoryItems = (ListView) findViewById(R.id.listView);
        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        inventoryItems.setEmptyView(emptyView);
        itemAdapter = new InventoryItemAdapter(this, null);
        inventoryItems.setAdapter(itemAdapter);

        inventoryItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                Uri uri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);
                intent.setData(uri);
                startActivity(intent);
            }
        });

        getLoaderManager().initLoader(INVENTORY_LOADER,null,this);
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
                return true;
            case R.id.action_delete_all_entries:
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void insertDummyData(){
        Uri uri;
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_INVENTORY_ITEM_NAME, "Phone");
        values.put(InventoryEntry.COLUMN_INVENTORY_ITEM_QUANTITY, 5);
        values.put(InventoryEntry.COLUMN_INVENTORY_ITEM_PRICE, 2.7);
        values.put(InventoryEntry.COLUMN_INVENTORY_ITEM_SUPPLIER_MAIL, "phone@phone.com");
        uri = getContentResolver().insert(InventoryEntry.CONTENT_URI,values);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String [] projection = {
            InventoryEntry._ID,
                InventoryEntry.COLUMN_INVENTORY_ITEM_NAME,
                InventoryEntry.COLUMN_INVENTORY_ITEM_PRICE,
                InventoryEntry.COLUMN_INVENTORY_ITEM_QUANTITY
        };

        //this loader will execute the Content Providers query method in the background
        return new CursorLoader(this,
                InventoryEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the inventory item.
                deleteAllItems();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the inventory item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the inventory item in the database.
     */
    private void deleteAllItems() {
        int rowsDeleted = getContentResolver().delete(InventoryEntry.CONTENT_URI,null,null);
        if(rowsDeleted<=0){
            Toast.makeText(this, R.string.main_delete_item_failed, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.main_delete_item_successful, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        itemAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        itemAdapter.swapCursor(null);
    }
}

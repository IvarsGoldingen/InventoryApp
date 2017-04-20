package com.example.android.inventoryapp;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.inventoryapp.data.InventoryDbHelper;
import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

/**
 * Created by Ivars on 2017.04.18..
 */

public class EditorActivity extends AppCompatActivity {

    private EditText mNameEditText;
    private EditText mPriceEditText;
    private EditText mSupplierMailEditText;
    private ImageView mImageView;
    private TextView mQuantity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_activity);

        mNameEditText = (EditText) findViewById(R.id.item_name);
        mPriceEditText = (EditText) findViewById(R.id.item_price);
        mSupplierMailEditText = (EditText) findViewById(R.id.email);
        mImageView = (ImageView) findViewById(R.id.item_image);
        mQuantity = (TextView) findViewById(R.id.product_quantity);
    }

    private void insertData(){
        String itemNameString = mNameEditText.getText().toString().trim();
        String itemPriceString = mPriceEditText.getText().toString().trim();
        float priceFloat = 0;
        if (!TextUtils.isEmpty(itemPriceString)){
            priceFloat = Float.parseFloat(itemPriceString);
        }
        String itemSupplierMailString = mSupplierMailEditText.getText().toString().trim();
        int quantityTest = 10;

        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_INVENTORY_ITEM_NAME, itemNameString);
        values.put(InventoryEntry.COLUMN_INVENTORY_ITEM_QUANTITY, quantityTest);
        values.put(InventoryEntry.COLUMN_INVENTORY_ITEM_PRICE, priceFloat);
        values.put(InventoryEntry.COLUMN_INVENTORY_ITEM_SUPPLIER_MAIL, itemSupplierMailString);
        //values.put(InventoryEntry.COLUMN_INVENTORY_ITEM_PICTURE, "");

        InventoryDbHelper mDbHelper = new InventoryDbHelper(this);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        long row = db.insert(InventoryEntry.TABLE_NAME, null, values);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_save:
                insertData();
                //finish will return the app to the main screen
                finish();
                return true;
            case R.id.action_delete:
                //do something
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

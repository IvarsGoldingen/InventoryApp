package com.example.android.inventoryapp;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

/**
 * Created by Ivars on 2017.04.22..
 */

public class InventoryItemAdapter extends CursorAdapter {

    public InventoryItemAdapter(Context context, Cursor c){
        super (context, c, 0);
    }

    /**
            * Makes a new blank list item view. No data is set (or bound) to the views yet.
            *
            * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
            */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.inventory_list_item, parent, false);
    }


    /**
     * This method binds the inventory data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current item can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView productNameView = (TextView)view.findViewById(R.id.product_name);
        TextView productPriceView = (TextView)view.findViewById(R.id.product_price);
        TextView productQuantityView = (TextView)view.findViewById(R.id.catalog_product_quantity);

        int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_ITEM_NAME);
        int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_ITEM_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_ITEM_QUANTITY);

        String itemName = cursor.getString(nameColumnIndex);
        float itemPrice = cursor.getFloat(priceColumnIndex);
        int itemQuantity = cursor.getInt(quantityColumnIndex);

        productNameView.setText(itemName);
        productPriceView.setText(context.getResources().getString(R.string.Main_act_price) +
                String.format(" %.2f", itemPrice) + " $");
        productQuantityView.setText(context.getResources().getString(R.string.main_quantity)+ " " + itemQuantity);
    }
}

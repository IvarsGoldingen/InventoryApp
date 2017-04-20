package com.example.android.inventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
  A class which holds the structure of the database
 since it holds only constants it is final
 */

public final class InventoryContract {

    /**
    public static final String CONTENT_AUTHORITY = "com.example.android.inventoryapp";
   public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    //???
    public static final String PATH_INVENTORY = "inventory";
    **/

    //empty constructor
    private InventoryContract() {
    }

    //inner class that defines the table
    public static class InventoryEntry implements BaseColumns {


//        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INVENTORY);
//
//        /**
//         * The MIME type of the {@link #CONTENT_URI} for a list of pets.
//         */
//        public static final String CONTENT_LIST_TYPE =
//                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;
//
//        /**
//         * The MIME type of the {@link #CONTENT_URI} for a single pet.
//         */
//        public static final String CONTENT_ITEM_TYPE =
//                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;


        public static final String TABLE_NAME = "inventory";

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_INVENTORY_ITEM_NAME = "name";
        public static final String COLUMN_INVENTORY_ITEM_QUANTITY = "quantity";
        public static final String COLUMN_INVENTORY_ITEM_PRICE = "price";
        public static final String COLUMN_INVENTORY_ITEM_SUPPLIER_MAIL = "mail";
        public static final String COLUMN_INVENTORY_ITEM_PICTURE = "image";
    }

}

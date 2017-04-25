package com.example.android.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

import org.apache.http.conn.ssl.StrictHostnameVerifier;

import static android.R.attr.id;
import static android.R.attr.name;
import static com.example.android.inventoryapp.data.InventoryContract.InventoryEntry.CONTENT_LIST_TYPE;

/**
 * Created by Ivars on 2017.04.22..
 */

public class InventoryProvider extends ContentProvider{

    /** URI matcher code for the content URI for the inventory table */
    private static final int INVENTORY = 100;

    /** URI matcher code for the content URI for a single inventory item in the pets table */
    private static final int INVENTORY_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        //The types of URIs that should be recognized
        //A full table uri
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY,INVENTORY);
        //a specific item uri
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY + "/#",INVENTORY_ID);

    }

    /** Tag for the log messages */
    public static final String LOG_TAG = InventoryProvider.class.getSimpleName();

    private InventoryDbHelper mDbHelper;

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        mDbHelper = new InventoryDbHelper(getContext());

        return false;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        //get readable database
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        //create cursor - values from the DB will be stored there
        Cursor cursor = null;
        //chack if the uri provided is valid
        //the store the appropriate values in the cursor
        switch (sUriMatcher.match(uri)){
            case INVENTORY:
                //get all the table
                cursor = db.query(InventoryEntry.TABLE_NAME,projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case INVENTORY_ID:
                //get a specific item from the table
                //SQL statement for this SELECT * FROM pets WHERE _id=5
                //the ? will be replaced by the values in selectionArgs
                selection = InventoryEntry._ID+"=?";
                selectionArgs = new String[]{
                        //get the 5 from for example com.example.android.inventory/inventory/5
                        String.valueOf(ContentUris.parseId(uri))
                };
                cursor = db.query(InventoryEntry.TABLE_NAME,projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw  new IllegalArgumentException("Unknown URI: " + uri);
        }
        //set notification URI on the cursor, so we know when to update the Cursor
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    /**
     * Returns the MIME type of data for the content URI.
     * MIME type can also be referred to as content type
     */
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return InventoryEntry.CONTENT_LIST_TYPE;
            case INVENTORY_ID:
                return InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case INVENTORY:
                return insertInventoryItem(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertInventoryItem(Uri uri, ContentValues values){
        // Check that the name is not null
        String name = values.getAsString(InventoryEntry.COLUMN_INVENTORY_ITEM_NAME);
        if (TextUtils.isEmpty(name)) {
            throw new IllegalArgumentException("Pet requires a name");
        }
        long price = values.getAsLong(InventoryEntry.COLUMN_INVENTORY_ITEM_PRICE);
        if (price<0) {
            throw new IllegalArgumentException("Price can not be negative");
        }
        int quantity = values.getAsInteger(InventoryEntry.COLUMN_INVENTORY_ITEM_QUANTITY);
        if (quantity<0) {
            throw new IllegalArgumentException("Quantity can not be negative");
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        long rowId = db.insert(InventoryEntry.TABLE_NAME, null, values);
        if (rowId == -1){
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        //notify listeners that data has changed
        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, rowId);
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowsDelted;
        final int match = sUriMatcher.match(uri);
        switch (match){
            case INVENTORY:
                // Delete all rows that match the selection and selection args
                rowsDelted = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case INVENTORY_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDelted = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        if (rowsDelted>0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDelted;
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);

        int rowsUpdated;

        switch (match){
            case INVENTORY:
                rowsUpdated = updateInventoryItem(uri, values, selection, selectionArgs);
                break;
            case INVENTORY_ID:
                // For the INVENTORY_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsUpdated = updateInventoryItem(uri, values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
        if (rowsUpdated>0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    private int updateInventoryItem (Uri uri, ContentValues values, String selection, String[] selectrionArgs){
        if (values.size() == 0) {
            //if nothing to update return
            return 0;
        }

        // Check that the name is not null
        if (values.containsKey(InventoryEntry.COLUMN_INVENTORY_ITEM_NAME)){
            String name = values.getAsString(InventoryEntry.COLUMN_INVENTORY_ITEM_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Pet requires a name");
            }
        }
        if (values.containsKey(InventoryEntry.COLUMN_INVENTORY_ITEM_NAME)) {
            long price = values.getAsLong(InventoryEntry.COLUMN_INVENTORY_ITEM_PRICE);
            if (price < 0) {
                throw new IllegalArgumentException("Price can not be negative");
            }
        }
        if (values.containsKey(InventoryEntry.COLUMN_INVENTORY_ITEM_NAME)) {
            int quantity = values.getAsInteger(InventoryEntry.COLUMN_INVENTORY_ITEM_QUANTITY);
            if (quantity < 0) {
                throw new IllegalArgumentException("Quantity can not be negative");
            }
        }



        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int rowsUpdated = db.update(
                InventoryEntry.TABLE_NAME,
                values,
                selection,
                selectrionArgs
        );
        return rowsUpdated;
    }
}

package com.example.android.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

import java.io.ByteArrayOutputStream;

/**
 * Created by Ivars on 2017.04.18..
 */

public class EditorActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int SELECT_PICTURE = 1;
    private static final int IMAGE_RESIZE_VALUE = 400;
    private static final int INVENTORY_LOADER=0;
    private Uri selectedImageUri = null;
    private Uri intentUri = null;

    private ImageView selectedImagePreview;
    private EditText mOrderQuantityText;
    private EditText mShipmentQuantityText;
    private EditText mNameEditText;
    private EditText mPriceEditText;
    private EditText mSupplierMailEditText;
    private TextView mQuantity;

    private Bitmap itemBitmap = null;

    //item quantity is a global variable so it can be updated using the record order shipment methods
    private int itemQuantity = 0;//default value

    //need for the discard changes option
    private boolean mItemHasChanged = false;
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_activity);

        mNameEditText = (EditText) findViewById(R.id.item_name);
        mPriceEditText = (EditText) findViewById(R.id.item_price);
        mSupplierMailEditText = (EditText) findViewById(R.id.email);
        mQuantity = (TextView) findViewById(R.id.items_in_stock);
        selectedImagePreview = (ImageView) findViewById(R.id.item_image);
        mOrderQuantityText = (EditText) findViewById(R.id.order_quantity_edit_field);
        mShipmentQuantityText = (EditText) findViewById(R.id.shipment_quantity_edit_field);

        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mSupplierMailEditText.setOnTouchListener(mTouchListener);

        intentUri = getIntent().getData();
        if (intentUri == null){
            setTitle("Add inventory item");
            invalidateOptionsMenu();
        }
        else{
            setTitle("Edit inventory item");
            getLoaderManager().initLoader(INVENTORY_LOADER,null,this);
        }

        Button selectImageButton = (Button) findViewById(R.id.select_image_button);
        selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mItemHasChanged = true;
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                //start an activity for which we want a result
                //when the activity exits onActivityResult() with the data is called
                startActivityForResult(Intent.createChooser(intent,
                        "Select Picture"), SELECT_PICTURE);
            }
        });
        selectImageButton.setOnTouchListener(mTouchListener);

        Button recordOrderButton = (Button) findViewById(R.id.record_order);
        recordOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordOrder();
            }
        });

        Button recordShipmentButton = (Button) findViewById(R.id.record_shipment);
        recordShipmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordShipment();
            }
        });

        Button orderItemByMailButton = (Button) findViewById(R.id.order_item_by_mail);
        orderItemByMailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMail();
            }
        });

        //notify user of these changes also in case of exiting without saving
        recordOrderButton.setOnTouchListener(mTouchListener);
        recordShipmentButton.setOnTouchListener(mTouchListener);

    }

    private void sendMail() {
        String[] mail = {mSupplierMailEditText.getText().toString().trim()};
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, mail);
        if (intent.resolveActivity(this.getPackageManager()) != null)
            startActivity(intent);
    }

    private void recordOrder(){
        String orderQuantityString = mOrderQuantityText.getText().toString();
        if (!TextUtils.isEmpty(orderQuantityString)){
            int orderQuantity = Integer.parseInt(orderQuantityString);
            if (orderQuantity<=itemQuantity){
                itemQuantity -= orderQuantity;
                refreshQuantityView();
            } else {
                Toast.makeText(this, getString(R.string.too_few_items_in_stock), Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void recordShipment(){
        String shipmentQuantityString = mShipmentQuantityText.getText().toString();
        if (!TextUtils.isEmpty(shipmentQuantityString)) {
            int shipmentQuantity = Integer.parseInt(shipmentQuantityString);
            itemQuantity += shipmentQuantity;
            refreshQuantityView();
        }
    }

    private void refreshQuantityView(){
        mQuantity.setText(String.valueOf(itemQuantity));
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //chack if getting the result is succesfull
        if (resultCode == RESULT_OK) {
            //check what are we getting the result for
            if (requestCode == SELECT_PICTURE) {
                //get the image URI
                selectedImageUri = data.getData();
                itemBitmap = decodeUri(selectedImageUri, IMAGE_RESIZE_VALUE);
                selectedImagePreview.setImageURI(selectedImageUri);
            }
        }
    }

    //COnvert and resize the image
    private Bitmap decodeUri(Uri selectedImage, int REQUIRED_SIZE) {
        try {
            // Decode image size
            BitmapFactory.Options opt1 = new BitmapFactory.Options();
            //by setting this to true, the bitmap is queried but not saved
            opt1.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, opt1);
            // Find the correct scale value. It should be the power of 2.
            int width_tmp = opt1.outWidth, height_tmp = opt1.outHeight;
            int scale = 1;
            while (true) {
                if (width_tmp / 2 < REQUIRED_SIZE
                        || height_tmp / 2 < REQUIRED_SIZE) {
                    break;
                }
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }
            // Decode with inSampleSize
            BitmapFactory.Options opt2 = new BitmapFactory.Options();
            //example, inSampleSize == 4 returns an image that is 1/4 the width/height of the original,
            Log.e("Scale:value: ", String.valueOf(scale));
            opt2.inSampleSize = scale;
            return BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, opt2);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private byte[] getByteArray(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        //put the bitmap into the array
        bitmap.compress(Bitmap.CompressFormat.JPEG, 0, stream);
        return stream.toByteArray();
    }

    private void saveData(){
        String itemNameString = mNameEditText.getText().toString().trim();
        if (TextUtils.isEmpty(itemNameString)){
            Toast.makeText(this, getString(R.string.item_must_have_name),
                    Toast.LENGTH_SHORT).show();
        }else{
            // if a item has a nami it can be saved, the price and quantity can be added later
            String itemPriceString = mPriceEditText.getText().toString().trim();
            float priceFloat = 0;
            if (!TextUtils.isEmpty(itemPriceString)){
                priceFloat = Float.parseFloat(itemPriceString);
            }
            String itemSupplierMailString = mSupplierMailEditText.getText().toString().trim();

            ContentValues values = new ContentValues();
            values.put(InventoryEntry.COLUMN_INVENTORY_ITEM_NAME, itemNameString);
            values.put(InventoryEntry.COLUMN_INVENTORY_ITEM_QUANTITY, itemQuantity);
            values.put(InventoryEntry.COLUMN_INVENTORY_ITEM_PRICE, priceFloat);
            values.put(InventoryEntry.COLUMN_INVENTORY_ITEM_SUPPLIER_MAIL, itemSupplierMailString);
            if (itemBitmap != null) {
                byte[] bitmapInBytes = getByteArray(itemBitmap);
                values.put(InventoryEntry.COLUMN_INVENTORY_ITEM_PICTURE, bitmapInBytes);
            }

            if (intentUri == null){
                //if a new inventory item is inserted
                Uri uri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);
                if (uri == null) {
                    // If the new content URI is null, then there was an error with insertion.
                    Toast.makeText(this, getString(R.string.editor_insert_item_failed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the insertion was successful and we can display a toast.
                    Toast.makeText(this, getString(R.string.editor_insert_item_successful),
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                //if a inventory item is updated
                int rowsUpdated = getContentResolver().update(intentUri, values, null, null);
                if (rowsUpdated == 0) {
                    // If no rows were updated, there was an error
                    Toast.makeText(this, getString(R.string.editor_update_item_failed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the update was successful and we can display a toast.
                    Toast.makeText(this, getString(R.string.editor_update_item_successful),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        //remove delete option on new inventory item
        if (intentUri == null){
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_save:
                saveData();
                //finish will return the app to the main screen
                finish();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                // If the inventory item hasn't changed, continue with navigating up to parent activity
                // which is the {@link MainActivity}.
                if (!mItemHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //All fields are show in the editor actity, so select everything from the table
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_INVENTORY_ITEM_NAME,
                InventoryEntry.COLUMN_INVENTORY_ITEM_PRICE,
                InventoryEntry.COLUMN_INVENTORY_ITEM_SUPPLIER_MAIL,
                InventoryEntry.COLUMN_INVENTORY_ITEM_QUANTITY,
                InventoryEntry.COLUMN_INVENTORY_ITEM_PICTURE
        };
        //selection does not need to be specidied because we ave the specific item's Uri in
        //the global variable intentUri
        return new CursorLoader(this,
                intentUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (data.moveToFirst()){//get to the first and only line, will return true if valid
            int nameColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_ITEM_NAME);
            int priceColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_ITEM_PRICE);
            int quantityColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_ITEM_QUANTITY);
            int mailColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_ITEM_SUPPLIER_MAIL);
            int imageColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_ITEM_PICTURE);

            String itemName = data.getString(nameColumnIndex);
            float itemPrice = data.getFloat(priceColumnIndex);
            itemQuantity = data.getInt(quantityColumnIndex);
            String itemSupplierMail = data.getString(mailColumnIndex);
            byte[] storedImage = data.getBlob(imageColumnIndex);

            mNameEditText.setText(itemName);
            mPriceEditText.setText(String.valueOf(itemPrice));
            mQuantity.setText(String.valueOf(itemQuantity));
            mSupplierMailEditText.setText(itemSupplierMail);
            if(storedImage != null){
                Bitmap image = convertByteToBitmap(storedImage);
                selectedImagePreview.setImageBitmap(image);
            }
        }
    }

    private Bitmap convertByteToBitmap(byte[] b){
        return BitmapFactory.decodeByteArray(b, 0, b.length);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mQuantity.setText("0");
        mSupplierMailEditText.setText("");
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
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

    @Override
    public void onBackPressed() {
        if(!mItemHasChanged){
            //if the item has not been changed just move back
            super.onBackPressed();
            return;
        }
        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };
        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the inventory item.
                deleteItem();
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
     * Perform the deletion of an item in the database.
     */
    private void deleteItem() {
        if (intentUri != null){
            int rowsDeleted = getContentResolver().delete(intentUri,null,null);
            if(rowsDeleted<=0){
                Toast.makeText(this, R.string.editor_delete_item_failed, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.editor_delete_item_successful, Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }
}

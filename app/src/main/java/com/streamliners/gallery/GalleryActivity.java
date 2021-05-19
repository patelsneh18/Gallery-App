package com.streamliners.gallery;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.streamliners.gallery.databinding.ActivityGalleryBinding;
import com.streamliners.gallery.databinding.ItemCardBinding;
import com.streamliners.gallery.models.Item;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {

    private static final int RESULT_LOAD_IMAGE = 0;
    ActivityGalleryBinding b;
    SharedPreferences preferences;
    List<Item> items = new ArrayList<>();
    private boolean isDialogBoxShowed = false;

    ItemCardBinding bindingToRemove;
    private List<String> urls = new ArrayList<>();
    private String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityGalleryBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        if (!items.isEmpty())
            b.noItemTV.setVisibility(View.GONE);

        preferences = getPreferences(MODE_PRIVATE);
        inflateDataFromSharedPreferences();
    }

    //Functions for Context Menu

    /**
     * Register Item for Context Menu
     * @param binding
     */

    private void registerForContextMenu(ItemCardBinding binding) {
        binding.imageView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                int index = b.list.indexOfChild(binding.getRoot());
                String mageUrl = urls.get(index-1);
                imageUrl = mageUrl;
                MenuInflater inflater = getMenuInflater();
                inflater.inflate(R.menu.edit_menu, menu);

                bindingToRemove = binding;
            }
        });
        binding.title.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                int index = b.list.indexOfChild(binding.getRoot());
                imageUrl = urls.get(index-1);
                MenuInflater inflater = getMenuInflater();
                inflater.inflate(R.menu.edit_menu, menu);

                bindingToRemove = binding;
            }
        });
    }


    /**
     * Handle Menu Item Selection
     * @param item
     * @return
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.editMenuItem) {

            new EditImageDialog()
                    .show(this, imageUrl, new EditImageDialog.onCompleteListener() {
                        @Override
                        public void onEditCompleted(Item item) {
                            int index = b.list.indexOfChild(bindingToRemove.getRoot()) - 1;
                            b.list.removeView(bindingToRemove.getRoot());
                            items.set(index, item);
                            //Inflate Layout
                            inflateViewAt(item,index+1);
                        }

                        @Override
                        public void onError(String error) {
                            new MaterialAlertDialogBuilder(GalleryActivity.this)
                                    .setTitle("Error")
                                    .setMessage(error)
                                    .show();
                        }
                    });
        }
        if (item.getItemId() == R.id.removeMenuItem){
            int index = b.list.indexOfChild(bindingToRemove.getRoot()) - 1;
            items.remove(index);
            b.list.removeView(bindingToRemove.getRoot());
            if (items.size() == 0)
                b.noItemTV.setVisibility(View.VISIBLE);

        }
        if (item.getItemId() == R.id.shareImage){
            shareImage();
        }
        return true;
    }

    private void shareImage() {
        int index = b.list.indexOfChild(bindingToRemove.getRoot()) - 1;
        Glide.with(this)
                .asBitmap()
                .load(urls.get(index))
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull @NotNull Bitmap resource, @Nullable @org.jetbrains.annotations.Nullable Transition<? super Bitmap> transition) {
                        Bitmap bitmap = resource;
                        String bitmapPath = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "palette", "share palette");
                        Uri bitmapUri = Uri.parse(bitmapPath);

                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("image/png");
                        intent.putExtra(Intent.EXTRA_STREAM, bitmapUri);
                        startActivity(Intent.createChooser(intent, "Share"));

                    }

                    @Override
                    public void onLoadCleared(@Nullable @org.jetbrains.annotations.Nullable Drawable placeholder) {

                    }
                });
    }

    /**
     * Gives Add Image Option in menu
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gallery, menu);
        return true;
    }


    /**
     * Shows add image dialog on clicking icon in menu
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.addImage){
            showAddImageDialog();
            return true;
        }
        if (item.getItemId() == R.id.addFromGallery){
            addFromGallery();
        }
        return false;
    }



    /**
     * Send Intent to get image from gallery
     */
    private void addFromGallery() {
        Intent i = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RESULT_LOAD_IMAGE);
    }


    /**
     * Shows Image Dialog Box
     */
    private void showAddImageDialog() {
        if (this.getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            isDialogBoxShowed = true;
            // To set the screen orientation in portrait mode only
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        new AddImageDialog()
                .show(this, new AddImageDialog.onCompleteListener() {
                    @Override
                    public void onImageAdded(Item item) {
                        items.add(item);
                        inflateViewforItem(item);
                        b.noItemTV.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(String error) {
                         new MaterialAlertDialogBuilder(GalleryActivity.this)
                                 .setTitle("Error")
                                 .setMessage(error)
                                 .show();
                    }
                });
    }


    /**
     * Adds Image Card to Linear Layout
     * @param item
     */
    private void inflateViewAt(Item item,int index) {

        //Inflate Layout
        ItemCardBinding binding = ItemCardBinding.inflate(getLayoutInflater());
        //Bind Data
        Glide.with(this)
                .asBitmap()
                .load(item.imageUrl)
                .into(binding.imageView);

        binding.title.setText(item.label);
        binding.title.setBackgroundColor(item.color);

        //Add it to the list

        b.list.addView(binding.getRoot(),index);
        registerForContextMenu(binding);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }

    /**
     * Adds Image Card to Linear Layout
     * @param item
     */
    private void inflateViewforItem(Item item) {

        //Inflate Layout
        ItemCardBinding binding = ItemCardBinding.inflate(getLayoutInflater());
        //Bind Data
        Glide.with(this)
                .asBitmap()
                .load(item.imageUrl)
                .into(binding.imageView);

        binding.title.setText(item.label);
        binding.title.setBackgroundColor(item.color);
        urls.add(item.imageUrl);

        //Add it to the list

        b.list.addView(binding.getRoot());
        registerForContextMenu(binding);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }


    /**
     * OverRide onPause method to save shared preferences
     */
    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences.Editor myEdit = preferences.edit();

        int numOfImg = items.size();
        myEdit.putInt(Constants.NUMOFIMG, numOfImg).apply();

        int counter = 0;
        for (Item item : items){
            myEdit.putInt(Constants.COLOR + counter, item.color)
                    .putString(Constants.LABEL + counter, item.label)
                    .putString(Constants.IMAGE + counter, urls.get(counter))
                    .apply();
            counter++;
        }
        myEdit.commit();
    }

    /**
     * Inflate data from shared preferences
     */
    private void inflateDataFromSharedPreferences(){
        int itemCount = preferences.getInt(Constants.NUMOFIMG,0);
        if (itemCount!=0) b.noItemTV.setVisibility(View.GONE);
        // Inflate all items from shared preferences
        for (int i = 0; i < itemCount; i++){

            Item item = new Item(preferences.getString(Constants.IMAGE + i,"")
                    ,preferences.getInt(Constants.COLOR + i,0)
                    ,preferences.getString(Constants.LABEL + i,""));

            items.add(item);
            inflateViewforItem(item);
        }
    }

    /**
     * Fetch image from gallery
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            String uri = selectedImage.toString();

            new AddFromGalleryDialog().show(this, uri, new AddFromGalleryDialog.onCompleteListener() {
                @Override
                public void onAddCompleted(Item item) {
                    items.add(item);
                    inflateViewforItem(item);
                    b.noItemTV.setVisibility(View.GONE);
                }

                @Override
                public void onError(String error) {
                    new MaterialAlertDialogBuilder(GalleryActivity.this)
                            .setTitle("Error")
                            .setMessage(error)
                            .show();
                }
            });
        }
    }


}
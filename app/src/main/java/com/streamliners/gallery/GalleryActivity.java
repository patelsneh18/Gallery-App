package com.streamliners.gallery;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
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

    private String imageUrl;
    ItemAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityGalleryBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        preferences = getPreferences(MODE_PRIVATE);
        inflateDataFromSharedPreferences();

        if (!items.isEmpty())
            showItems(items);

    }

    //Functions for Context Menu

    /**
     * Handle Menu Item Selection
     *
     * @param item
     * @return
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        imageUrl = adapter.imageUrl;    //Image Url of Parent of Context Menu
        int index = adapter.index;      //Index of item for context menu
        ItemCardBinding binding = adapter.itemCardBinding;      //Binding of parent of context menu
        if (item.getItemId() == R.id.editMenuItem) {
            new EditImageDialog()
                    .show(this, imageUrl, new EditImageDialog.onCompleteListener() {
                        @Override
                        public void onEditCompleted(Item item) {
//                            int index = b.list.indexOfChild(bindingToRemove.getRoot()) - 1;
                            items.set(index, item);
                            //Inflate Layout
                            adapter.notifyDataSetChanged();
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
        if (item.getItemId() == R.id.shareImage)
            shareImage(binding);
        return true;
    }

    private void shareImage(ItemCardBinding binding) {
        Bitmap bitmap = getBitmapFromView(binding.getRoot());
        String bitmapPath = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "palette", "share palette");
        Uri bitmapUri = Uri.parse(bitmapPath);
        //Intent to send image
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/png");
        intent.putExtra(Intent.EXTRA_STREAM, bitmapUri);
        startActivity(Intent.createChooser(intent, "Share"));
    }

    /**
     * Returns Bitmap from a View
     *
     * @param view
     * @return
     */
    public static Bitmap getBitmapFromView(View view) {
        //Define a bitmap with the same size as the view
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        //Bind a canvas to it
        Canvas canvas = new Canvas(returnedBitmap);
        //Get the view's background
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null)
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        else
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE);
        // draw the view on the canvas
        view.draw(canvas);
        //return the bitmap
        return returnedBitmap;
    }

    /**
     * Gives Add Image Option in menu
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gallery, menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();

        // SearchView on query text listener to add search function of adapter
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return true;
            }
        });
        return true;
    }


    /**
     * Shows add image dialog on clicking icon in menu
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.addImage) {
            showAddImageDialog();
            return true;
        }
        if (item.getItemId() == R.id.addFromGallery) {
            addFromGallery();
        }
        if (item.getItemId() == R.id.sortLabels) {
            adapter.sortAlphabetically();
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
//            isDialogBoxShowed = true;
            // To set the screen orientation in portrait mode only
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        new AddImageDialog()
                .show(this, new AddImageDialog.onCompleteListener() {
                    @Override
                    public void onImageAdded(Item item) {
                        items.add(item);
                        showItems(items);

//                        b.noItemTV.setVisibility(View.GONE);
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

    // Callback for swipe action
    ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull @NotNull RecyclerView recyclerView, @NonNull @NotNull RecyclerView.ViewHolder viewHolder, @NonNull @NotNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull @NotNull RecyclerView.ViewHolder viewHolder, int direction) {
            items.remove(viewHolder.getAdapterPosition());
            adapter.notifyDataSetChanged();
        }
    };

    /**
     * Pass items list to ItemAdapter and add additional callbacks
     *
     * @param items
     */
    public void showItems(List<Item> items) {
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        adapter = new ItemAdapter(this, items);
        b.list.setLayoutManager(new LinearLayoutManager(this));

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        adapter.setItemAdapterHelper(itemTouchHelper);
        itemTouchHelper.attachToRecyclerView(b.list);
        ItemTouchHelper.Callback callback2 = new ItemAdapterHelper(adapter);
        ItemTouchHelper itemTouchHelper1 = new ItemTouchHelper(callback2);
        adapter.setItemAdapterHelper(itemTouchHelper1);
        itemTouchHelper1.attachToRecyclerView(b.list);
        b.list.setAdapter(adapter);
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
        for (Item item : items) {
            myEdit.putInt(Constants.COLOR + counter, item.color)
                    .putString(Constants.LABEL + counter, item.label)
                    .putString(Constants.IMAGE + counter, item.imageUrl)
                    .apply();
            counter++;
        }
        myEdit.commit();
    }

    /**
     * Inflate data from shared preferences
     */
    private void inflateDataFromSharedPreferences() {
        int itemCount = preferences.getInt(Constants.NUMOFIMG, 0);
//        if (itemCount!=0) b.noItemTV.setVisibility(View.GONE);
        // Inflate all items from shared preferences
        for (int i = 0; i < itemCount; i++) {

            Item item = new Item(preferences.getString(Constants.IMAGE + i, "")
                    , preferences.getInt(Constants.COLOR + i, 0)
                    , preferences.getString(Constants.LABEL + i, ""));

            items.add(item);
        }
        showItems(items);
    }

    /**
     * Fetch image from gallery
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
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
                    showItems(items);
//                    b.noItemTV.setVisibility(View.GONE);
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
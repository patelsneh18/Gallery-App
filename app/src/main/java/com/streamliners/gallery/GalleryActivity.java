package com.streamliners.gallery;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ActionBar;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.streamliners.gallery.adapters.ItemAdapter;
import com.streamliners.gallery.databinding.ActivityGalleryBinding;
import com.streamliners.gallery.databinding.ItemCardBinding;
import com.streamliners.gallery.helpers.ItemAdapterHelper;
import com.streamliners.gallery.models.Item;

import org.jetbrains.annotations.NotNull;

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
    int mode = 0;       //If mode=1 Drag Enabled  Else Drag Disabled
    ItemTouchHelper.Callback callback2;
    ItemTouchHelper itemTouchHelper1;
    Context context = this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityGalleryBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());


        preferences = getPreferences(MODE_PRIVATE);
        inflateDataFromSharedPreferences();

        if (!items.isEmpty())
            showItems(items);
        else b.noItems.setVisibility(View.VISIBLE);
        enableDisableDrag();

        getSupportActionBar().setTitle((Html.fromHtml("<font color=\"#FFFFFF\">" + getString(R.string.app_name) + "</font>")));

    }

    /**
     * On Click Listener Floating Button
     */
    void enableDisableDrag(){
        b.dragListener.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mode == 0){
                    mode = 1;
                    adapter.mode = 1;
                    List<ItemAdapter.ItemHolder> holders = adapter.holderList;
                    b.dragListener.setBackgroundTintList(getResources().getColorStateList(R.color.clicked));
                    b.dragListener.setRippleColor(getResources().getColorStateList(R.color.not_clicked));

                    b.dragListener.setImageResource(R.drawable.ic_vertical);
                    for (int i=0; i<holders.size(); i++){
                        holders.get(i).listenerSetter();
                    }

                    itemTouchHelper1.attachToRecyclerView(b.list);
                }
                else {
                    mode = 0;
                    adapter.mode = 0;
                    List<ItemAdapter.ItemHolder> holders = adapter.holderList;
                    for (int i=0; i<holders.size(); i++){
                        holders.get(i).listenerSetter();
                    }
                    b.dragListener.setBackgroundTintList(getResources().getColorStateList(R.color.not_clicked));
                    b.dragListener.setRippleColor(getResources().getColorStateList(R.color.clicked));
                    b.dragListener.setImageResource(R.drawable.ic_vertical_not);
                    itemTouchHelper1.attachToRecyclerView(null);
                }
            }
        });
    }

    /**
     * Restore Drag Mode or Non-Drag Mode from Shared Preferences
     */
    void modeRestore(){
        if (mode == 1){
            mode = 1;
            adapter.mode = 1;
            List<ItemAdapter.ItemHolder> holders = adapter.holderList;
            b.dragListener.setBackgroundTintList(getResources().getColorStateList(R.color.clicked));
            b.dragListener.setRippleColor(getResources().getColorStateList(R.color.not_clicked));

            b.dragListener.setImageResource(R.drawable.ic_vertical);
            for (int i=0; i<holders.size(); i++){
                holders.get(i).listenerSetter();
            }

            itemTouchHelper1.attachToRecyclerView(b.list);
        }
        else {
            mode = 0;
            adapter.mode = 0;
            List<ItemAdapter.ItemHolder> holders = adapter.holderList;
            for (int i=0; i<holders.size(); i++){
                holders.get(i).listenerSetter();
            }
            b.dragListener.setBackgroundTintList(getResources().getColorStateList(R.color.not_clicked));
            b.dragListener.setRippleColor(getResources().getColorStateList(R.color.clicked));
            b.dragListener.setImageResource(R.drawable.ic_vertical_not);
            itemTouchHelper1.attachToRecyclerView(null);
        }
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
        Intent share = new Intent(Intent.ACTION_SEND);
//        String bitmapPath = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "palette", "share palette");
//        Uri bitmapUri = Uri.parse(bitmapPath);
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "title");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values);


        OutputStream outputStream;
        try {
            outputStream = getContentResolver().openOutputStream(uri);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.close();
        } catch (Exception e) {
            System.err.println(e.toString());
        }

        share.putExtra(Intent.EXTRA_STREAM, uri);
        //Intent to send image

        share.setType("image/png");
        share.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(share, "Share"));
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
            String uri = selectedImage.toString();
            //Show Add Image Dialog
            new AddImageDialog()
                    .fetchDataForGallery(uri, this, new AddImageDialog.onCompleteListener() {
                        @Override
                        public void onImageAdded(Item item) {
                            items.add(item);
                            showItems(items);

                            b.noItems.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError(String error) {

                        }
                    });
        }
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

                        b.noItems.setVisibility(View.GONE);
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
            Toast.makeText(context, "Image Removed", Toast.LENGTH_SHORT).show();
            if (items.isEmpty())
                b.noItems.setVisibility(View.VISIBLE);
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

        //Item Touch Helper for Swipe to Remove
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        adapter.setItemAdapterHelper(itemTouchHelper);
        itemTouchHelper.attachToRecyclerView(b.list);
        callback2 = new ItemAdapterHelper(adapter);

        //Item Touch Helper for Drag and Drop
        itemTouchHelper1 = new ItemTouchHelper(callback2);
        adapter.setItemAdapterHelper(itemTouchHelper1);

        b.list.setAdapter(adapter);
        //Restore Mode
        modeRestore();
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
        myEdit.putInt(Constants.MODE, mode);
        myEdit.commit();
    }

    /**
     * Inflate data from shared preferences
     */
    private void inflateDataFromSharedPreferences() {
        int itemCount = preferences.getInt(Constants.NUMOFIMG, 0);
        if (itemCount!=0) b.noItems.setVisibility(View.GONE);
        // Inflate all items from shared preferences
        for (int i = 0; i < itemCount; i++) {

            Item item = new Item(preferences.getString(Constants.IMAGE + i, "")
                    , preferences.getInt(Constants.COLOR + i, 0)
                    , preferences.getString(Constants.LABEL + i, ""));

            items.add(item);
        }
        mode = preferences.getInt(Constants.MODE, 0);
        showItems(items);
    }


}
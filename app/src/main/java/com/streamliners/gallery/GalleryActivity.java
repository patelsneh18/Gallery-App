package com.streamliners.gallery;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.streamliners.gallery.databinding.ActivityGalleryBinding;
import com.streamliners.gallery.databinding.ChipColorBinding;
import com.streamliners.gallery.databinding.ChipLabelBinding;
import com.streamliners.gallery.databinding.ItemCardBinding;
import com.streamliners.gallery.models.Item;

import java.util.List;
import java.util.Set;

public class GalleryActivity extends AppCompatActivity {

    ActivityGalleryBinding b;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityGalleryBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

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
        return false;
    }


    /**
     * Shows Image Dialog Box
     */
    private void showAddImageDialog() {
        new AddImageDialog()
                .show(this, new AddImageDialog.onCompleteListener() {
                    @Override
                    public void onImageAdded(Item item) {
                        inflateViewforItem(item);
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
    private void inflateViewforItem(Item item) {
        //Inflate Layout
        ItemCardBinding binding = ItemCardBinding.inflate(getLayoutInflater());
        //Bind Data
        binding.imageView.setImageBitmap(item.image);
        binding.title.setText(item.label);
        binding.title.setBackgroundColor(item.color);

        //Add it to the list
        b.list.addView(binding.getRoot());
    }
}
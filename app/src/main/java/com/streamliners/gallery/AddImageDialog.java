package com.streamliners.gallery;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.streamliners.gallery.databinding.ChipColorBinding;
import com.streamliners.gallery.databinding.ChipLabelBinding;
import com.streamliners.gallery.databinding.DialogAddImageBinding;
import com.streamliners.gallery.models.Item;

import java.util.List;
import java.util.Set;

public class AddImageDialog implements ItemHelper.OnCompleteListener {
    private Context context;
    private onCompleteListener listener;
    private DialogAddImageBinding b;
    private LayoutInflater inflater;

    private boolean isCustomLabel;
    private Bitmap image;
    private AlertDialog dialog;


    /**
     * Inflate Dialogs Layout
     * @param context
     * @param listener
     */
    void show(Context context , onCompleteListener listener){
        this.context = context;
        this.listener = listener;
        if (context instanceof GalleryActivity){
            inflater = ((GalleryActivity) context).getLayoutInflater();
            b = DialogAddImageBinding.inflate(inflater);
        }
        else {
            dialog.dismiss();
            listener.onError("Cast Exception");
            return;
        }

        //Create and Show Dialog
        dialog = new MaterialAlertDialogBuilder(context)
                .setView(b.getRoot())
                .show();

        //Handle Events
        handleDimensionsInput();
        hideErrorsForET();
    }

    //Utils

    /**
     * Hide Error of text field on text change
     */
    private void hideErrorsForET() {
        b.width.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                b.width.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    // Step 1: Input Dimensions

    /**
     * Takes Input of Height and Width and fetches image
     */
    private void handleDimensionsInput() {
        b.fetchimageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String widthStr = b.width.getText().toString().trim()
                        , heightStr = b.height.getText().toString().trim();

                //Update Ui
                b.inputDimensionRoot.setVisibility(View.GONE);
                b.progressIndicatorRoot.setVisibility(View.VISIBLE);

                hideKeyboard();
                //Guard Code
                if (widthStr.isEmpty() && heightStr.isEmpty()){
                    b.width.setError("Please enter atleast on dimension");
                    return;
                }  //Square Img
                else if (widthStr.isEmpty()){
                    int height = Integer.parseInt(heightStr);
                    fetchRandomImage(height);
                }  //Square Img
                else if(heightStr.isEmpty()){
                    int width = Integer.parseInt(widthStr);
                    fetchRandomImage(width);
                }  //Rectangular Img
                else {
                    int height = Integer.parseInt(heightStr);
                    int width = Integer.parseInt(heightStr);
                    fetchRandomImage(width, height);
                }
            }
        });
    }

    /**
     * Hide Keyboard after taking input of width and height
     */
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);

        imm.hideSoftInputFromWindow(b.width.getWindowToken(), 0);
    }

    //Step 2: Fetch Random Image

    /**
     * Fetch Square Image
     * @param x
     */
    private void fetchRandomImage(int x) {
        new ItemHelper()
                .fetchData(x , context, this);
    }

    /**
     * Fetch Rectangular Image
     * @param width
     * @param height
     */
    private void fetchRandomImage(int width, int height) {
        new ItemHelper()
                .fetchData(width, height, context, this);
    }

    //Step: 3 Show Data

    /**
     * Show Data on Main Root to Add Image to Gallert
     * @param image
     * @param colors
     * @param labels
     */
    private void showData(Bitmap image, Set<Integer> colors, List<String> labels) {
        this.image = image;
        b.mainRoot.setVisibility(View.VISIBLE);
        b.customLabelInput.setVisibility(View.GONE);
        b.imageView.setImageBitmap(image);
        inflateColorChips(colors);
        inflateLabelChips(labels);
        handleCustomLabelInput();
        handleAddImageEvent();
    }


    /**
     * Takes Custom Label Input
     */
    private void handleCustomLabelInput(){
        ChipLabelBinding binding = ChipLabelBinding.inflate(inflater);
        binding.getRoot().setText("Custom");
        b.labelChips.addView(binding.getRoot());

        binding.getRoot().setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    b.customLabelInput.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                    isCustomLabel = isChecked;
            }
        });
    }


    /**
     * inflates color chips to main root
     * @param colors
     */
    private void inflateColorChips(Set<Integer> colors){
        for (int color: colors){
            ChipColorBinding binding = ChipColorBinding.inflate(inflater);
            binding.getRoot().setChipBackgroundColor(ColorStateList.valueOf(color));
            b.colorChips.addView(binding.getRoot());
        }
    }


    /**
     * Inflates label chips to main root
     * @param labels
     */
    private void inflateLabelChips(List<String> labels){
        for (String label: labels){
            ChipLabelBinding binding = ChipLabelBinding.inflate(inflater);
            binding.getRoot().setText(label);
            b.labelChips.addView(binding.getRoot());
        }
    }


    /**
     * Handle actions when user clicks on add image button
     */
    private void handleAddImageEvent() {
        b.addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int colorChipId = b.colorChips.getCheckedChipId(),
                        labelChipId = b.labelChips.getCheckedChipId();

                if (colorChipId == -1 || labelChipId == -1) {
                    Toast.makeText(context, "Please choose color & label", Toast.LENGTH_SHORT).show();
                    return;
                }
                String label;
                if (isCustomLabel){
                    label = b.customLabelEt.getText().toString().trim();
                    if (label.isEmpty()){
                        Toast.makeText(context,"Please enter custom label",Toast.LENGTH_SHORT).show();
                        return;
                    }
                }else {
                    label = ((Chip) b.labelChips.findViewById(labelChipId)).getText().toString();
                }
                int color = ((Chip) b.colorChips.findViewById(colorChipId)).getChipBackgroundColor().getDefaultColor();

                //Send Callback
                Item item = new Item(image, color, label);
                listener.onImageAdded(item);
                dialog.dismiss();
            }
        });
    }

    @Override
    public void onFetchedData(Bitmap image, Set<Integer> colors, List<String> labels) {
        b.progressIndicatorRoot.setVisibility(View.GONE);
        showData(image, colors, labels);
    }

    @Override
    public void onError(String error) {
        listener.onError(error);
        dialog.dismiss();
    }


    /**
     * callbacks for add image completion
     */
    interface onCompleteListener{
        void onImageAdded(Item item);
        void onError(String error);

    }
}

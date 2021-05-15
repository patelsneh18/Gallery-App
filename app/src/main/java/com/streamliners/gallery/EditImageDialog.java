package com.streamliners.gallery;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.palette.graphics.Palette;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;
import com.streamliners.gallery.databinding.ChipColorBinding;
import com.streamliners.gallery.databinding.ChipLabelBinding;
import com.streamliners.gallery.databinding.DialogEditImageBinding;
import com.streamliners.gallery.models.Item;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EditImageDialog {
    private Context context;
    private EditImageDialog.onCompleteListener listener;
    private DialogEditImageBinding b;
    private LayoutInflater inflater;

    private boolean isCustomLabel;
    public Bitmap image;
    private AlertDialog dialog;
    Set<Integer> colors;


    /**
     * Show Edit Image Dialog Box
     * @param context
     * @param image
     * @param listener
     */
    public void show(Context context,Bitmap image, EditImageDialog.onCompleteListener listener){
        this.listener = listener;
        this.image = image;
        this.context = context;
        if (context instanceof GalleryActivity){
            inflater = ((GalleryActivity) context).getLayoutInflater();
            b = DialogEditImageBinding.inflate(inflater);
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

        extractPaletteFromBitmap();
        updateNewColorAndLabel();
    }

    private void extractLabels() {
        InputImage inputImage = InputImage.fromBitmap(image,0);
        ImageLabeler labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);
        labeler.process(inputImage)
                .addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
                    @Override
                    public void onSuccess(@NonNull @NotNull List<ImageLabel> imageLabels) {
                        List<String> strings = new ArrayList<>();
                        for (ImageLabel label : imageLabels){
                            strings.add(label.getText());
                        }
                        inflateColorChips(colors);
                        inflateLabelChips(strings);
                        b.edImageView.setImageBitmap(image);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        listener.onError(e.toString());
                    }
                });
    }

    private void extractPaletteFromBitmap() {
        Palette.from(image).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                colors = getColorsFromPalette(palette);
                extractLabels();
            }
        });
    }

    private void updateNewColorAndLabel() {
        b.updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int colorChipId = b.edColorChips.getCheckedChipId(),
                        labelChipId = b.edLabelChips.getCheckedChipId();

                if (colorChipId == -1 || labelChipId == -1) {
                    Toast.makeText(context, "Please choose color & label", Toast.LENGTH_SHORT).show();
                    return;
                }
                String label;
                if (isCustomLabel){
                    label = b.edCustomLabelEt.getText().toString().trim();
                    if (label.isEmpty()){
                        Toast.makeText(context,"Please enter custom label",Toast.LENGTH_SHORT).show();
                        return;
                    }
                }else {
                    label = ((Chip) b.edLabelChips.findViewById(labelChipId)).getText().toString();
                }
                int color = ((Chip) b.edColorChips.findViewById(colorChipId)).getChipBackgroundColor().getDefaultColor();

                //Send Callback
                Item item = new Item(image, color, label);
                listener.onEditCompleted(item);
                dialog.dismiss();
            }
        });
    }

    private Set<Integer> getColorsFromPalette (Palette palette) {
        Set<Integer> colors = new HashSet<>();
        colors.add(palette.getVibrantColor(0));
        colors.add(palette.getLightVibrantColor(0));
        colors.add(palette.getDarkVibrantColor(0));

        colors.add(palette.getMutedColor(0));
        colors.add(palette.getLightMutedColor(0));
        colors.add(palette.getDarkMutedColor(0));

        colors.add(palette.getVibrantColor(0));
        colors.remove(0);

        return colors;
    }

    /**
     * inflates color chips to main root
     */
    private void inflateColorChips(Set<Integer> colors){
        //Inflate color chips to edit dialog
        for (int color: colors){
            ChipColorBinding binding = ChipColorBinding.inflate(inflater);
            binding.getRoot().setChipBackgroundColor(ColorStateList.valueOf(color));
            b.edColorChips.addView(binding.getRoot());
        }

    }


    /**
     * Inflates label chips to main root
     */
    private void inflateLabelChips(List<String> labels){


        //Inflate label chips to edit dialog
        for (String label: labels){
            ChipLabelBinding binding = ChipLabelBinding.inflate(inflater);
            binding.getRoot().setText(label);
            b.edLabelChips.addView(binding.getRoot());
        }
    }

    /**
     * callbacks for edit completion
     */
    interface onCompleteListener{
        void onEditCompleted(Item item);
        void onError(String error);

    }
}

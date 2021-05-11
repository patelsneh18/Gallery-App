package com.streamliners.gallery.models;

import android.graphics.Bitmap;

/**
 * Properties to inflate basic gallery layout for an image
 */
public class Item {
    public Bitmap image;
    public int color;
    public String label;

    
    /**
     * Parameterised constructor for item class
     * @param image
     * @param color
     * @param label
     */
    public Item(Bitmap image, int color, String label) {
        this.image = image;
        this.color = color;
        this.label = label;
    }
}

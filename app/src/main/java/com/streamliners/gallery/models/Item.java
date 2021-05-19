package com.streamliners.gallery.models;

import android.graphics.Bitmap;

/**
 * Properties to inflate basic gallery layout for an image
 */
public class Item {
    public String imageUrl;
    public int color;
    public String label;

    
    /**
     * Parameterised constructor for item class
     * @param imageUrl
     * @param color
     * @param label
     */
    public Item(String imageUrl, int color, String label) {
        this.imageUrl = imageUrl;
        this.color = color;
        this.label = label;
    }
}

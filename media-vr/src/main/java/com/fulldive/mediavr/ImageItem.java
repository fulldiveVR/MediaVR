package com.fulldive.mediavr;

import java.util.ArrayList;

/**
 * Created by managerdark on 23.02.2015.
 */
public class ImageItem {
    public final long id;
    public String data;
    public String title;
    public String bucket;
    public int width;
    public int height;
    public boolean isSpherical;

    public float inner_width = 0;
    public float inner_height = 0;
    public double inner_angle = 0;
    public double inner_angle_offset = 0;
    public ArrayList<ImageItem> imagesList = null;

    public ImageItem(long id, final String data, final int width, final int height) {
        this.id = id;
        this.data = data;
        this.width = width;
        this.height = height;
        this.title = null;
        this.bucket = null;
        this.isSpherical = width >= 2048 && height >= 1024 && (height * 2 == width);
    }
    public ImageItem(long id, final String data, final String title, final int width, final int height) {
        this.id = id;
        this.data = data;
        this.width = width;
        this.height = height;
        this.title = title;
        this.bucket = null;
        this.isSpherical = width >= 2048 && height >= 1024 && (height * 2 == width);
    }

    public ImageItem(final long id, final String title, final String bucket) {
        this.id = id;
        this.title = title;
        this.bucket = bucket;
        this.data = null;
        imagesList = new ArrayList<>();
        isSpherical = false;
    }

    public void update() {
        if (imagesList != null) {
            for (ImageItem item : imagesList) {
                if (item.isSpherical) {
                    data = item.data;
                    width = item.width;
                    height = item.height;
                    isSpherical = true;
                    break;
                } else if (data == null) {
                    data = item.data;
                    width = item.width;
                    height = item.height;
                }
            }
        }
    }
}

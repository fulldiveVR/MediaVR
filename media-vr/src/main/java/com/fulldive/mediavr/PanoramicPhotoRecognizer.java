package com.fulldive.mediavr;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.fulldive.basevr.utils.FdLog;

import java.util.Arrays;
import java.util.Locale;

/**
 * Created by Dmitry Chernov <managerdark@gmail.com> on 23.11.15.
 */
public class PanoramicPhotoRecognizer {
    private static final String TAG = PanoramicPhotoRecognizer.class.getSimpleName();
    private static int BLOCK_SIZE = 20;
    private static int MIN_HEIGHT = 400;
    public static float OPTIMAL_THRESHOLD = 0.98f;

    public static float recognize(final Bitmap bitmap, final float frame_threshold) {
        float result = 0f;
        if (bitmap != null) {
            final int bitmap_width = bitmap.getWidth();
            final int bitmap_height = bitmap.getHeight();

            if (bitmap_height >= MIN_HEIGHT && bitmap_width >= bitmap_height) {
                final int blocks_count = Math.round((float) bitmap_height / (float) BLOCK_SIZE);

                if (FdLog.isAvailable())
                    FdLog.d(TAG, String.format(Locale.ENGLISH, "w: %d   h: %d   block: %d", bitmap_width, bitmap_height, blocks_count));
                final int left_edge[] = getPixelsColumn(bitmap, 0);
                final int right_edge[] = getPixelsColumn(bitmap, bitmap_width - 1);
                if (FdLog.isAvailable()) {
                    FdLog.d(TAG, "left_edge: " + Arrays.toString(left_edge));
                    FdLog.d(TAG, "right_edge: " + Arrays.toString(right_edge));
                }
                final Block left_blocks = pixelsToBlocks(left_edge, BLOCK_SIZE, blocks_count);
                final Block right_blocks = pixelsToBlocks(right_edge, BLOCK_SIZE, blocks_count);
                if (FdLog.isAvailable()) {
                    FdLog.d(TAG, "left_blocks, min: " + left_blocks.min_color_intensity + "  max: " + left_blocks.max_color_intensity + " colors: " + Arrays.toString(left_blocks.colors));
                    FdLog.d(TAG, "right_blocks, min: " + right_blocks.min_color_intensity + " max: " + right_blocks.max_color_intensity + " colors: " + Arrays.toString(right_blocks.colors));
                    FdLog.d(TAG, "frame_threshold: " + frame_threshold + " => " + (int) (255f * frame_threshold) + " diff_left: " + (left_blocks.max_color_intensity - left_blocks.min_color_intensity) + " diff_right: " + (right_blocks.max_color_intensity - right_blocks.min_color_intensity));
                }
                if (left_blocks.max_color_intensity - left_blocks.min_color_intensity >= (int) (255f * frame_threshold) || right_blocks.max_color_intensity - right_blocks.min_color_intensity >= (int) (255f * frame_threshold)) {
                    result = compare(left_blocks.colors, right_blocks.colors);
                }
            }
        }

        return result;
    }

    private static float compare(final float[] left, final float[] right) {
        final int count = Math.min(left.length, right.length);
        final double max_value = Math.sqrt(Math.pow(255d, 2d) * 3d);
        float sum = 0;
        int index = 0;
        for (int i = 0; i < count; i += 3) {
            final double value = Math.sqrt(Math.pow(left[i] - right[i], 2d) + Math.pow(left[i + 1] - right[i + 1], 2d) + Math.pow(left[i + 2] - right[i + 2], 2d));
            sum += (float) (value / max_value);
            if (FdLog.isAvailable())
                FdLog.d(TAG, " value: " + value + " / " + max_value);
            ++index;
        }
        return 1f - (index > 0 ? sum / (float) index : 1f);
    }

    private static Block pixelsToBlocks(int[] pixels, int blockSize, int blocksCount) {
        final float[] result = new float[blocksCount * 3]; // RRGGBBRRGGBB... format
        final int count = pixels.length;
        int block_index = 0;
        int r = 0;
        int g = 0;
        int b = 0;
        float min_color_intensity = 255;
        float max_color_intensity = 0;

        int index = 0;
        for (int i = 0; i < count && block_index < blocksCount; i++) {
            final int color = pixels[i];
            final int tr = Color.red(color);
            final int tg = Color.green(color);
            final int tb = Color.blue(color);

            r = (index == 0) ? tr : (r + tr);
            g = (index == 0) ? tg : (g + tg);
            b = (index == 0) ? tb : (b + tb);

            final int grayscale = Math.round((float) (tr + tg + tb) / 3f);
            min_color_intensity = Math.min(min_color_intensity, grayscale);
            max_color_intensity = Math.max(max_color_intensity, grayscale);
            ++index;
            if (index == blockSize || (i == count - 1)) {
                final int e = block_index * 3;
                result[e] = (float) r / (float) index;
                result[e + 1] = (float) g / (float) index;
                result[e + 2] = (float) b / (float) index;
                ++block_index;
                index = 0;
            }
        }

        return new Block(result, min_color_intensity, max_color_intensity);
    }

    private static int[] getPixelsColumn(final Bitmap bitmap, final int column) {
        final int height = bitmap.getHeight();
        final int pixels[] = new int[height];

        bitmap.getPixels(pixels, 0, 1, column, 0, 1, height);

        return pixels;
    }

    public static class Block {
        public float colors[] = null;
        public float min_color_intensity = 0;
        public float max_color_intensity = 0;

        public Block(final float[] colors, final float min_color_intensity, final float max_color_intensity) {
            this.colors = colors;
            this.min_color_intensity = min_color_intensity;
            this.max_color_intensity = max_color_intensity;
        }
    }
}

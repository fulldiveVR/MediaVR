package com.fulldive.mediavr.scenes.vree;


import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.fulldive.basevr.components.SkyboxItem;
import com.fulldive.basevr.controls.Control;
import com.fulldive.basevr.controls.ImageControl;
import com.fulldive.basevr.controls.ImageProvider;
import com.fulldive.basevr.controls.OnControlClick;
import com.fulldive.basevr.controls.OnControlSelect;
import com.fulldive.basevr.controls.TextboxControl;
import com.fulldive.basevr.events.SoundEvent;
import com.fulldive.basevr.framework.ActionsScene;
import com.fulldive.basevr.framework.FulldiveContext;
import com.fulldive.basevr.framework.Scene;
import com.fulldive.mediavr.ImageItem;
import com.fulldive.mediavr.MenuAdapter;
import com.fulldive.mediavr.R;
import com.fulldive.mediavr.SpiralMenuControl;

import java.util.ArrayList;

public class VreeScene extends ActionsScene implements MenuAdapter, OnControlClick, OnControlSelect {
    private final static String TAG_TUTORIAL = "tutorial_vree";
    private SpiralMenuControl menuControl = null;
    private ArrayList<ImageItem> sourceBucketsList = new ArrayList<>();
    private ArrayList<ImageItem> bucketsList = new ArrayList<>();
    private TextboxControl emptyLabel = null;
    private TextboxControl imageLabel = null;
    private boolean isShowSphericalOnly = true;
    private boolean isHomeButtonVisible = true;
    private boolean tutorialShown = false;
    private SkyboxItem cachedSkybox = null;

    public VreeScene(@NonNull final FulldiveContext fulldiveContext) {
        super(fulldiveContext);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // lock screen
        setRangeY((float) (Math.PI / 2d));
        setInactiveSceneDistance(30f);

        ImageControl imageControl = new ImageControl();
        imageControl.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.mediavr_menu_bar));
        imageControl.setSize(30f, 0.08f);
        imageControl.setPivot(.5f, .5f);
        imageControl.setPosition(0, -8, 0f);
        imageControl.setSortIndex(20);
        addControl(imageControl);


        imageLabel = new TextboxControl();
        imageLabel.setSize(30f, 1.5f);
        imageLabel.setSortIndex(11);
        imageLabel.setPivot(.5f, .5f);
        imageLabel.setPosition(0, -10f, 1f);
        imageLabel.setGravityCenter();
        imageLabel.setBackgroundColor(Color.TRANSPARENT);
        addControl(imageLabel);

        menuControl = new SpiralMenuControl.Builder(getFulldiveContext()).build(R.drawable.mediavr_menu_select);
        menuControl.setAdapter(this);
        emptyLabel = new TextboxControl();
        emptyLabel.setSize(30f, 1f);
        emptyLabel.setPivot(.5f, .5f);
        emptyLabel.setPosition(0, 0, 0f);
        emptyLabel.setGravityCenter();
        emptyLabel.setBackgroundColor(Color.TRANSPARENT);
        emptyLabel.setText(getString(R.string.mediavr_loading));
        menuControl.setEmptyControl(emptyLabel);
        menuControl.setOnItemSelectedListener(this);
        menuControl.setOnClickListener(this);
        addControl(menuControl);


        new Thread(new Runnable() {
            @Override
            public void run() {
                getBuckets();
                updateBuckets();
            }
        }).start();

    }

    private ArrayList<ImageItem> doFilter(final ArrayList<ImageItem> items) {
        ArrayList<ImageItem> result;
        if (!isShowSphericalOnly) {
            result = new ArrayList<>(items);
        } else {
            result = new ArrayList<>();
            for (ImageItem item : items
                    ) {
                if (item.isSpherical) {
                    result.add(item);
                }
            }
        }
        return result;
    }

    private void updateBuckets() {
        bucketsList = doFilter(sourceBucketsList);
        if (bucketsList.isEmpty()) {
            emptyLabel.setText(getString(R.string.mediavr_empty));
        }
        if (menuControl != null)
            menuControl.notifyDatasetChanged();
    }

    @Override
    public boolean onClick(@Nullable Control control) {
        if (!super.onClick(control)) {
            getEventBus().post(new SoundEvent(SoundEvent.SOUND_CLICK2));
            menuControl.click();
        }
        return true;
    }


    private void getBuckets() {
        sourceBucketsList.clear();
        ArrayList<ImageItem> bucketsList = new ArrayList<>();

        final Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        final ContentResolver contentResolver = getResourcesManager().getContext().getContentResolver();
        // Make the query.
        Cursor cursor = null;


        // which image properties are we querying
        final String[] PROJECTION_BUCKET = {
                MediaStore.Images.Media.BUCKET_ID,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media.DATE_TAKEN,
                MediaStore.Images.Media._ID};
        final String BUCKET_GROUP_BY = "1) GROUP BY 1,(2";
        final String BUCKET_ORDER_BY = "MAX(datetaken) DESC";

        try {
            cursor = contentResolver.query(images, PROJECTION_BUCKET, BUCKET_GROUP_BY, null, BUCKET_ORDER_BY);
            Log.i("ListingImages", " query count=" + (cursor != null ? cursor.getCount() : -1));

            if (cursor != null && cursor.moveToFirst()) {
                final int idColumn = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                final int bucketColumn = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
                final int bucketIdColumn = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID);
                do {
                    bucketsList.add(new ImageItem(cursor.getInt(idColumn), cursor.getString(bucketColumn), cursor.getString(bucketIdColumn)));
                } while (cursor.moveToNext());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e("ListingImages", ex.toString());

        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        for (ImageItem item : bucketsList) {
            if (getImages(item, contentResolver)) {
                item.update();
                sourceBucketsList.add(item);
            }
        }
    }

    private boolean getImages(final ImageItem bucket, final ContentResolver contentResolver) {
        bucket.imagesList.clear();

        final Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        // Make the query.
        Cursor cursor = null;


        final String[] PROJECTION_BUCKET = {MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.DATA, MediaStore.Images.ImageColumns.WIDTH, MediaStore.Images.ImageColumns.HEIGHT, MediaStore.Images.ImageColumns.DISPLAY_NAME};
        try {
            final String select = MediaStore.Images.ImageColumns.BUCKET_ID + " = ?";
            cursor = contentResolver.query(
                    images, PROJECTION_BUCKET, select, new String[]{bucket.bucket},
                    MediaStore.Images.Media.DATE_TAKEN + " DESC");  // Sort by the latest files.

            if (cursor != null && cursor.moveToFirst()) {
                int width;
                int height;
                long id;
                String data;
                String title;
                int idColumn = cursor.getColumnIndex(
                        MediaStore.Images.Media._ID);
                int widthColumn = cursor.getColumnIndex(
                        MediaStore.Images.Media.WIDTH);
                int heightColumn = cursor.getColumnIndex(
                        MediaStore.Images.Media.HEIGHT);

                int dataColumn = cursor.getColumnIndex(
                        MediaStore.Images.Media.DATA);
                int titleColumn = cursor.getColumnIndex(
                        MediaStore.Images.Media.DISPLAY_NAME);

                do {
                    id = cursor.getLong(idColumn);
                    width = cursor.getInt(widthColumn);
                    height = cursor.getInt(heightColumn);
                    data = cursor.getString(dataColumn);
                    title = cursor.getString(titleColumn);
                    final ImageItem item = new ImageItem(id, data, title, width, height);
                    bucket.imagesList.add(item);
                } while (cursor.moveToNext());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e("ListingImages", ex.toString());

        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return !bucket.imagesList.isEmpty();
    }

    @Override
    public int getCount() {
        return bucketsList.size();
    }

    @Override
    public Control createControl() {
        return new ImageControl();
    }

    @Override
    public void bindControl(final Control control, final int position) {
        final ImageControl button = (ImageControl) control;
        final ImageItem imageItem = bucketsList.get(position);
        button.setUid(position);
        final Context context = getResourcesManager().getContext();
        final ContentResolver contentResolver = context.getContentResolver();
        button.setImageProvider(new ImageProvider() {
            @Override
            public Bitmap getImage() {
                Bitmap res = MediaStore.Images.Thumbnails.getThumbnail(contentResolver,
                        imageItem.id, MediaStore.Images.Thumbnails.MINI_KIND, null);
                if (null == res)
                    res = BitmapFactory.decodeResource(context.getResources(),
                            R.drawable.mediavr_no_preview);
                return res;
            }
        });
    }

    @Override
    public Object getItem(final int position) {
        return bucketsList.get(position);
    }

    @Override
    public void click(final Control control) {
        final int uid = (int) control.getUid();
        if (uid >= 0 && uid < bucketsList.size()) {
            final ImageItem imageItem = bucketsList.get(uid);
            final VreeImagesScene scene = new VreeImagesScene(getFulldiveContext());
            scene.setImages(imageItem.imagesList);
            show(scene);
        }
    }

    @Override
    public void selected(final Control control) {
        final int uid = (int) control.getUid();
        if (uid >= 0 && uid < bucketsList.size()) {
            final ImageItem imageItem = bucketsList.get(uid);
            imageLabel.setText(imageItem.title);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        cachedSkybox = getSceneManager().getSkybox();
        getSceneManager().setSkybox(null);
        setAlpha(0f);
        setTargetAlpha(1f);
    }

    @Override
    public void onStop() {
        getSceneManager().setSkybox(cachedSkybox);
        super.onStop();
    }

    @Override
    protected ArrayList<ActionItem> getActions() {
        final ArrayList<ActionItem> result = new ArrayList<>();
        if (isHomeButtonVisible)
            result.add(new ActionItem(0, R.drawable.home_normal, R.drawable.home_pressed, getString(R.string.common_actionbar_home)));
        if (isShowSphericalOnly)
            result.add(new ActionItem(1, R.drawable.mediavr_panoramic_normal, R.drawable.mediavr_panoramic_pressed, getString(R.string.mediavr_actionbar_panoramic)));
        else
            result.add(new ActionItem(1, R.drawable.mediavr_spherical_normal, R.drawable.mediavr_spherical_pressed, getString(R.string.mediavr_actionbar_spherical)));
        result.add(new ActionItem(2, R.drawable.tutorial_icon_normal, R.drawable.tutorial_icon_pressed, getString(R.string.common_actionbar_tutorial)));
        return result;
    }

    @Override
    public void onActionClicked(final int action) {
        super.onActionClicked(action);
        switch (action) {
            case 0:
                onBack();
                break;
            case 1:
                isShowSphericalOnly = !isShowSphericalOnly;
                updateActions();
                updateBuckets();
                break;
            case 2:
                showTutorial();
                break;
        }
    }

    @Override
    public void onUpdate(long timeMs) {
        super.onUpdate(timeMs);
        menuControl.setEnable(!isActionsVisible() && !hasCurrentDialogue());
    }

    @Override
    public boolean isTutorialShown() {
        if (!tutorialShown) {
            tutorialShown = true;
            return getResourcesManager().getProperty(TAG_TUTORIAL, false);
        }
        return true;
    }

    public Scene getTutorial() {
        VreeTutorialScene scene = new VreeTutorialScene(getFulldiveContext());
        scene.setTag(TAG_TUTORIAL);
        return scene;
    }

    public void setHomeButtonVisible(final boolean isVisible) {
        isHomeButtonVisible = isVisible;
    }
}

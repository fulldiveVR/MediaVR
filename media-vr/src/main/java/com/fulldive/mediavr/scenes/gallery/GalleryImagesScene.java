package com.fulldive.mediavr.scenes.gallery;


import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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
import com.fulldive.mediavr.ImageItem;
import com.fulldive.mediavr.MenuAdapter;
import com.fulldive.mediavr.R;
import com.fulldive.mediavr.SpiralMenuControl;

import java.util.ArrayList;

/**
 * Created by managerdark on 24.09.2015.
 */
// list of images
public class GalleryImagesScene extends ActionsScene implements MenuAdapter, OnControlClick, OnControlSelect {
    private SpiralMenuControl menu = null;
    private ArrayList<ImageItem> imagesList = new ArrayList<>();
    private TextboxControl emptyLabel = null;
    private TextboxControl imageLabel = null;
    private SkyboxItem cachedSkybox = null;

    public GalleryImagesScene(@NonNull final FulldiveContext fulldiveContext) {
        super(fulldiveContext);
    }

    public void setImages(final ArrayList<ImageItem> imagesList) {
        this.imagesList = new ArrayList<>(imagesList);
        if (this.imagesList.isEmpty()) {
            emptyLabel.setText(getString(R.string.mediavr_empty));
        }
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

        menu = new SpiralMenuControl.Builder(getFulldiveContext()).build(R.drawable.mediavr_menu_select);
        menu.setSize(20f, 20f);
        menu.setPivot(0.5f, 0.5f);
        menu.setAdapter(this);
        menu.setFocusEventsMode(Control.FOCUS_EVENTS_MODE_ALL);
        emptyLabel = new TextboxControl();
        emptyLabel.setSize(30f, 1f);
        emptyLabel.setPivot(.5f, .5f);
        emptyLabel.setPosition(0, 0, 0f);
        emptyLabel.setGravityCenter();
        emptyLabel.setBackgroundColor(Color.TRANSPARENT);
        emptyLabel.setText(getString(R.string.mediavr_loading));
        menu.setEmptyControl(emptyLabel);
        menu.setOnItemSelectedListener(this);
        menu.setOnClickListener(this);
        addControl(menu);
    }

    @Override
    public boolean onClick(@Nullable Control control) {
        if (!super.onClick(control)) {
            getEventBus().post(new SoundEvent(SoundEvent.SOUND_CLICK2));
            menu.click();
        }
        return true;
    }

    @Override
    public int getCount() {
        return imagesList.size();
    }

    @Override
    public Control createControl() {
        return new ImageControl();
    }

    @Override
    public void bindControl(final Control control, final int position) {
        final ImageControl button = (ImageControl) control;
        final ImageItem imageItem = imagesList.get(position);
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
        return imagesList.get(position);
    }

    @Override
    public void click(final Control control) {
        final int uid = (int) control.getUid();
        if (uid >= 0 && uid < imagesList.size()) {
            final ImageItem imageItem = imagesList.get(uid);
            final GalleryFullImageScene scene = new GalleryFullImageScene(getFulldiveContext());
            scene.setImage(imageItem);
            show(scene);
        }
    }

    @Override
    public void selected(final Control control) {
        final int uid = (int) control.getUid();
        if (uid >= 0 && uid < imagesList.size()) {
            final ImageItem imageItem = imagesList.get(uid);
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
        result.add(new ActionItem(0, R.drawable.back_normal, R.drawable.back_pressed, getString(R.string.common_actionbar_back)));
        return result;
    }

    @Override
    public void onActionClicked(final int action) {
        super.onActionClicked(action);
        switch (action) {
            case 0:
                onBack();
                break;
        }
    }

    @Override
    public void onUpdate(long timeMs) {
        super.onUpdate(timeMs);
        menu.setEnable(!isActionsVisible() && !hasCurrentDialogue());
    }
}

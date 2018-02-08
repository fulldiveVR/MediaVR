package com.fulldive.mediavr.scenes.vree;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.animation.Interpolator;

import com.fulldive.basevr.components.SharedTexture;
import com.fulldive.basevr.components.SkyboxItem;
import com.fulldive.basevr.controls.Control;
import com.fulldive.basevr.controls.Entity;
import com.fulldive.basevr.controls.SphereImageControl;
import com.fulldive.basevr.events.SoundEvent;
import com.fulldive.basevr.framework.ActionsScene;
import com.fulldive.basevr.framework.ControlsBuilder;
import com.fulldive.basevr.framework.FulldiveContext;
import com.fulldive.basevr.framework.ParentProvider;
import com.fulldive.basevr.framework.Utilities;
import com.fulldive.basevr.framework.animation.Animation;
import com.fulldive.mediavr.ImageItem;
import com.fulldive.mediavr.R;

import java.util.ArrayList;

/**
 * Created by managerdark on 24.09.2015.
 */
public class VreeFullImageScene extends ActionsScene {
    private static final String TAG = VreeFullImageScene.class.getSimpleName();

    private ImageItem imageItem = null;
    private SharedTexture sharedTexture = new SharedTexture();
    private SkyboxItem cachedSkybox = null;

    public VreeFullImageScene(@NonNull final FulldiveContext fulldiveContext) {
        super(fulldiveContext);
    }

    public void setImage(final ImageItem image) {
        imageItem = image;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setRangeY((float) (Math.PI / 2d));
        setActiveSceneDistance(0f);
        final Context context = getResourcesManager().getContext();
        final SphereImageControl sphereImageControl = new SphereImageControl();
        ControlsBuilder.setBaseProperties(sphereImageControl, 0f, 0f, 0f, .5f, .5f, .5f, 99f, 99f, 99f);
        sphereImageControl.setSlices(48);
        sphereImageControl.setStacks(48);
        sphereImageControl.setAlpha(0f);
        sphereImageControl.setAutoClick(false);
        sphereImageControl.setSharedTexture(sharedTexture);
        addControl(sphereImageControl);
        sphereImageControl.setParent(new ParentProvider() {
            public float getAlpha() {
                return VreeFullImageScene.this.getAlpha();
            }

            @Override
            public FulldiveContext getFulldiveContext() {
                return VreeFullImageScene.this.getFulldiveContext();
            }

            @Override
            public Animation startAnimation(Animation animation, Entity target, String tag, Interpolator interpolator) {
                return VreeFullImageScene.this.startAnimation(animation, target, tag, interpolator);
            }

            @Override
            public void stopAnimation(String tag) {
                VreeFullImageScene.this.stopAnimation(tag);
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean stopTrying = false;
                double coefficient = 1.0;
                Bitmap bitmap = null;
                while (!stopTrying && coefficient > 0.1) {
                    try {
                        // First decode with inJustDecodeBounds=true to check dimensions
                        final BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;
                        BitmapFactory.decodeFile(imageItem.data, options);

                        // Calculate inSampleSize
                        final int maxSize = (int) (4096.0 * coefficient);
                        options.inSampleSize = Utilities.calculateInSampleSize(options, maxSize, maxSize);
                        // Decode bitmap with inSampleSize set
                        options.inJustDecodeBounds = false;

                        bitmap = BitmapFactory.decodeFile(imageItem.data, options);
                        stopTrying = true;
                    } catch (OutOfMemoryError oom) {
                        if (null != bitmap) bitmap.recycle();
                        //decrease size of image we try to load
                        coefficient = coefficient * 0.75;
                        System.gc();
                    }
                }
                try {
                    if (null == bitmap) {
                        bitmap = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable.mediavr_no_preview);
                    }
                } catch (OutOfMemoryError outOfMemoryError) {
                    System.gc();
                }

                try {
                    if (bitmap != null && sharedTexture != null) {
                        sharedTexture.setBitmap(bitmap);
                        sphereImageControl.setTargetAlpha(1f);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public boolean onClick(@Nullable Control control) {
        if (!super.onClick(control)) {
            getEventBus().post(new SoundEvent(SoundEvent.SOUND_CLICK2));
            if (!isActionsVisible()) {
                dismiss();
            }
        }
        return true;
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
    public void onDestroy() {
        sharedTexture.deleteTexture();
        super.onDestroy();
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
}

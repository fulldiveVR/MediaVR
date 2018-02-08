package com.fulldive.mediavr.scenes.gallery;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;

import com.fulldive.basevr.components.SharedTexture;
import com.fulldive.basevr.components.SkyboxItem;
import com.fulldive.basevr.controls.Control;
import com.fulldive.basevr.controls.OnControlClick;
import com.fulldive.basevr.controls.OnControlFocus;
import com.fulldive.basevr.controls.PanoramicImageControl;
import com.fulldive.basevr.controls.SphereImageControl;
import com.fulldive.basevr.controls.ViewControl;
import com.fulldive.basevr.events.ActivityStatusEvent;
import com.fulldive.basevr.events.PermissionsRequestEvent;
import com.fulldive.basevr.events.SoundEvent;
import com.fulldive.basevr.framework.ActionsScene;
import com.fulldive.basevr.framework.ControlsBuilder;
import com.fulldive.basevr.framework.FulldiveContext;
import com.fulldive.basevr.framework.Utilities;
import com.fulldive.mediavr.ImageItem;
import com.fulldive.mediavr.R;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;

/**
 * Created by managerdark on 24.09.2015.
 */
// Full size image view
public class GalleryFullImageScene extends ActionsScene {
    private final static int STATUS_EMPTY = 0;
    private final static int STATUS_LOADED = 1;
    private int status = STATUS_EMPTY;

    private EventBus eventBus = EventBus.getDefault();
    private ImageItem image = null;
    private boolean isSpherical = false;
    private SphereImageControl sphereImageControl = null;
    private PanoramicImageControl panoramicImageControl = null;
    private SharedTexture sharedTexture = new SharedTexture();
    private ViewControl permissionRequireButton;
    private SkyboxItem cachedSkybox = null;

    public GalleryFullImageScene(@NonNull final FulldiveContext fulldiveContext) {
        super(fulldiveContext);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        sphereImageControl = new SphereImageControl();
        ControlsBuilder.setBaseProperties(sphereImageControl, 0f, 0f, 0f, .5f, .5f, .5f, 99f, 99f, 99f);
        sphereImageControl.setSlices(48);
        sphereImageControl.setStacks(48);
        sphereImageControl.setAlpha(0f);
        sphereImageControl.setSharedTexture(sharedTexture);
        addControl(sphereImageControl);

        panoramicImageControl = new PanoramicImageControl();
        ControlsBuilder.setBaseProperties(panoramicImageControl, 0f, 0f, 0f, .5f, .5f, .5f, 40f, 20f, 40f);
        panoramicImageControl.setAlpha(0f);
        panoramicImageControl.setSharedTexture(sharedTexture);
        addControl(panoramicImageControl);

        permissionRequireButton = new ViewControl(getFulldiveContext());
        ControlsBuilder.setBaseProperties(permissionRequireButton, getWidth() / 2, getHeight() / 2 + 1.5f, -1f, .5f, .5f, 6f, 1.2f);
        permissionRequireButton.setFixedSize(6f, 1.2f);
        permissionRequireButton.setLayoutId(R.layout.permissions_require_button);
        permissionRequireButton.setOnInflateListener(new ViewControl.OnViewInflateListener() {
            @Override
            public void OnViewInflated(@NonNull View view) {
                ((TextView) permissionRequireButton.findViewById(R.id.title)).setText(
                        getString(R.string.common_permission_require_button));
            }
        });
        permissionRequireButton.setOnFocusListener(new OnControlFocus() {
            @Override
            public void onControlFocused(Control control) {
                permissionRequireButton.setTargetScale(1.1f);
            }

            @Override
            public void onControlUnfocused(Control control) {
                permissionRequireButton.setTargetScale(1f);
            }
        });
        permissionRequireButton.setOnClickListener(new OnControlClick() {
            @Override
            public void click(Control control) {
                eventBus.post(new PermissionsRequestEvent());
            }
        });
        permissionRequireButton.setAlpha(0f);
        addControl(permissionRequireButton);

        if (hasPermissions()) {
            setImageToControl();
        }

        updateActions();
        updateControl();

        if (!eventBus.isRegistered(this)) {
            eventBus.register(this);
        }
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
        if (isSpherical)
            result.add(new ActionItem(1, R.drawable.mediavr_panoramic_normal, R.drawable.mediavr_panoramic_pressed, getString(R.string.mediavr_actionbar_panoramic)));
        else
            result.add(new ActionItem(1, R.drawable.mediavr_spherical_normal, R.drawable.mediavr_spherical_pressed, getString(R.string.mediavr_actionbar_spherical)));
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
                isSpherical = !isSpherical;
                updateControl();
                break;
        }
    }

    public void setImage(final ImageItem image) {
        this.image = image;
    }

    public void setIsSpherical(boolean value) {
        isSpherical = value;
    }

    public boolean getIsSpherical() {
        return isSpherical;
    }

    @Override
    public void onDestroy() {
        sphereImageControl = null;
        panoramicImageControl = null;
        sharedTexture.deleteTexture();
        if (eventBus.isRegistered(this)) {
            eventBus.unregister(this);
        }
        super.onDestroy();
    }

    private void updateControl() {
        if (!hasPermissions()) {
            panoramicImageControl.setAlpha(0f);
            sphereImageControl.setAlpha(0f);
            permissionRequireButton.setAlpha(1f);
            return;
        }
        permissionRequireButton.setAlpha(0f);

        if (isSpherical) {
            sphereImageControl.setTargetAlpha(1f);
            panoramicImageControl.setAlpha(0f);
        } else {
            panoramicImageControl.setTargetAlpha(1f);
            sphereImageControl.setAlpha(0f);
        }
        updateActions();
    }

    private void setImageToControl() {
        setActiveSceneDistance(0f);

        final Context context = getResourcesManager().getContext();
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
                        BitmapFactory.decodeFile(image.data, options);

                        // Calculate inSampleSize
                        final int maxSize = (int) (4096.0 * coefficient);
                        options.inSampleSize = Utilities.calculateInSampleSize(options, maxSize, maxSize);
                        // Decode bitmap with inSampleSize set
                        options.inJustDecodeBounds = false;

                        bitmap = BitmapFactory.decodeFile(image.data, options);
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
                } catch (OutOfMemoryError oom) {
                    System.gc();
                }

                try {
                    if (sphereImageControl != null && panoramicImageControl != null && bitmap != null && sharedTexture != null) {
                        sharedTexture.setBitmap(bitmap);
                        image.width = bitmap.getWidth();
                        image.height = bitmap.getHeight();
                        isSpherical = image.isSpherical;
                        updateControl();
                        status = STATUS_LOADED;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).start();
        isSpherical = image.isSpherical;
    }

    private boolean hasPermissions() {
        return ContextCompat.checkSelfPermission(getResourcesManager().getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressWarnings("unused")
    public void onEvent(final ActivityStatusEvent event) {
        if (event.isForeground() && hasPermissions() && status == STATUS_EMPTY) {
            setImageToControl();
        }
    }
}

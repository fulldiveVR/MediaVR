package com.fulldive.mediavr.scenes.camera;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.fulldive.basevr.controls.Control;
import com.fulldive.basevr.events.SoundEvent;
import com.fulldive.basevr.framework.ActionsScene;
import com.fulldive.basevr.framework.FulldiveContext;
import com.fulldive.basevr.framework.ParentProvider;
import com.fulldive.basevr.framework.Scene;
import com.fulldive.mediavr.DisplayControl;
import com.fulldive.mediavr.R;
import com.fulldive.mediavr.scenes.gallery.GalleryScene;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by managerdark on 20.10.2015.
 */
public class CameraScene extends ActionsScene {
    private static final String TAG = CameraScene.class.getSimpleName();
    public static final String PREFERENCE_TAG = "SAVE_DIR";
    public static final String PARAM_TARGET = "TARGET";
    private final static String TAG_TUTORIAL = "tutorial_camera";
    private static Camera camera = null;
    private final float displaySize = 50f;
    private boolean tutorialShown = false;
    private DisplayControl displayControl = null;
    private SurfaceTexture surfaceTexture;
    private int availableFrames = 0;
    private boolean phoneTarget = true;
    private boolean cameraInitFailed = false;
    private boolean isHomeButtonVisible = true;

    private boolean isPreviewStarted = false;

    private CameraProvider cameraProvider = new CameraProvider();

    private class CameraProvider extends ParentProvider {
        @Override
        public FulldiveContext getFulldiveContext() {
            return CameraScene.this.getFulldiveContext();
        }
    }

    public CameraScene(@NonNull final FulldiveContext fulldiveContext) {
        super(fulldiveContext);
        restoreSettings();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setRangeY((float) ((Math.PI) / 2d));

        displayControl = new DisplayControl();
        displayControl.setPosition(0f, 0f, 26f);
        displayControl.setSize(displaySize, displaySize);
        displayControl.setPivot(.5f, .5f);
        addControl(displayControl);
        displayControl.setParent(cameraProvider);
    }

    @Override
    public void onUpdate(long timeMs) {
        if (cameraInitFailed) {
            return;
        }
        displayControl.setVisible(getCurrentDialogue() == null);
        try {
            if (availableFrames > 0) {
                surfaceTexture.updateTexImage();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        availableFrames = 0;

        super.onUpdate(timeMs);
    }

    protected boolean initCamera() {
        if (camera != null) return true;

        try {
            camera = Camera.open();
            final Camera.Size size = camera.getParameters().getPreviewSize();
            final float k = (float) size.width / (float) size.height;
            displayControl.setSize(displaySize * k, displaySize);
            surfaceTexture = displayControl.getSurfaceTexture();
            surfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
                @Override
                public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                    availableFrames++;
                }
            });
            try {
                camera.setPreviewTexture(surfaceTexture);
            } catch (IOException e) {
                Log.e(TAG, "Setting preview texture: " + e);
            }

            camera.startPreview();
            this.isPreviewStarted = true;
            availableFrames = 0;

            Log.d(TAG, "initCamera success: " + camera);
            return true;
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
            ex.printStackTrace();
            camera = null;
        }
        return false;
    }

    @Override
    public void onStart() {
        super.onStart();
        getSceneManager().setSkybox(null);
        cameraInitFailed = !initCamera();
    }

    @Override
    public void onStop() {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
        super.onStop();
    }


    @Override
    protected ArrayList<ActionItem> getActions() {
        final ArrayList<ActionItem> result = new ArrayList<>();
        if (isHomeButtonVisible) {
            result.add(new ActionItem(0, R.drawable.home_normal, R.drawable.home_pressed, getString(R.string.common_actionbar_home)));
        }

        result.add(new ActionItem(1, R.drawable.mediavr_panoramic_normal, R.drawable.mediavr_panoramic_pressed, getString(R.string.mediavr_actionbar_panoramic)));
        if (phoneTarget) {
            result.add(new ActionItem(2, R.drawable.mediavr_phone_normal, R.drawable.mediavr_phone_pressed, getString(R.string.mediavr_actionbar_phone)));
        } else {
            result.add(new ActionItem(2, R.drawable.mediavr_sd_normal, R.drawable.mediavr_sd_pressed, getString(R.string.mediavr_actionbar_sdcard)));
        }
        result.add(new ActionItem(3, R.drawable.tutorial_icon_normal, R.drawable.tutorial_icon_pressed, getString(R.string.common_actionbar_tutorial)));

        return result;
    }

    @Override
    public boolean onClick(@Nullable Control control) {
        if (!super.onClick(control)) {
            if (ContextCompat.checkSelfPermission(getResourcesManager().getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                getSceneManager().showPermissionDialog();
            } else {
                takePicture();
            }
        }
        return true;
    }

    @Override
    public void onActionClicked(int action) {
        super.onActionClicked(action);
        switch (action) {
            case 0:
                onBack();
                break;
            case 1: {
                final GalleryScene scene = new GalleryScene(getFulldiveContext());
                scene.setBackButton(true);
                show(scene);
            }
            break;
            case 2: {
                phoneTarget = !phoneTarget;
                updateActions();
                saveSettings();
            }
            break;
            case 3:
                showTutorial();
                break;
        }
    }


    private void takePicture() {
        if (camera != null && this.isPreviewStarted) {
            this.isPreviewStarted = false;
            camera.autoFocus(new Camera.AutoFocusCallback() {
                public void onAutoFocus(boolean success, Camera camera) {
                    CameraScene.camera.takePicture(null, null, new Camera.PictureCallback() {
                        @Override
                        public void onPictureTaken(byte[] data, Camera camera) {
                            getEventBus().post(new SoundEvent(SoundEvent.SOUND_CAMERA_CLICK));
                            File file = null;
                            try {
                                file = createImageFile();
                            } catch (IOException e) {
                                Log.e(TAG, "Create file: " + e);
                            }
                            if (null != file) {
                                try {
                                    FileOutputStream fos = new FileOutputStream(file);
                                    fos.write(data);
                                    fos.flush();
                                    fos.getFD().sync();
                                    fos.close();
                                } catch (Exception e) {
                                    Log.e(TAG, "Save file: " + e);
                                }
                            }
                            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                            Uri contentUri = Uri.fromFile(file);
                            mediaScanIntent.setData(contentUri);
                            getResourcesManager().getContext().sendBroadcast(mediaScanIntent);
                            camera.startPreview();
                            CameraScene.this.isPreviewStarted = true;
                        }
                    });
                }
            });
        }
    }

    @Override
    protected void fixRotate(float[] euler) {
        super.fixRotate(euler);
        cameraProvider.setPreRotateX((double) euler[0]);
        cameraProvider.setPreRotateY(-(double) euler[1]);
        cameraProvider.setPreRotateZ(-(double) euler[2]);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
        String imageFileName = "Shot_" + timeStamp + "_";
        File storageDir;
        if (phoneTarget) {
            storageDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DCIM), "Camera");
        } else {
            storageDir = new File(Environment.getExternalStorageDirectory(), "FullDive");
        }

        if (!storageDir.exists()) {
            storageDir.mkdir();
        }
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
    }

    public void restoreSettings() {
        final SharedPreferences sharedPreferences = getResourcesManager().getContext().getSharedPreferences(PREFERENCE_TAG, Context.MODE_PRIVATE);
        phoneTarget = sharedPreferences.getBoolean(PARAM_TARGET, true);
//        updateActions();
    }

    public void saveSettings() {
        try {
            final SharedPreferences.Editor sharedPreferences = getResourcesManager().getContext().getSharedPreferences(PREFERENCE_TAG, Context.MODE_PRIVATE).edit();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        sharedPreferences.putBoolean(PARAM_TARGET, phoneTarget).apply();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        Log.e(TAG, ex.toString());
                    }
                }
            }).start();
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e(TAG, ex.toString());
        }
    }

    public void setHomeButtonVisible(final boolean isVisible) {
        isHomeButtonVisible = isVisible;
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
        CameraTutorialScene scene = new CameraTutorialScene(getFulldiveContext());
        scene.setTag(TAG_TUTORIAL);
        return scene;
    }

    public void permissionsGranted() {
        cameraInitFailed = !initCamera();
    }
}

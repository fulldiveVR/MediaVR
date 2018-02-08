package com.fulldive.mediavr.scenes.camera;

import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.fulldive.basevr.controls.FrameLayout;
import com.fulldive.basevr.controls.ImageControl;
import com.fulldive.basevr.framework.FulldiveContext;
import com.fulldive.basevr.framework.TutorialScene;
import com.fulldive.basevr.utils.VrConstants;
import com.fulldive.mediavr.R;

/**
 * Created by managerdark on 22.10.15.
 */
public class CameraTutorialScene extends TutorialScene {
    private String tag = null;

    public CameraTutorialScene(@NonNull final FulldiveContext fulldiveContext) {
        super(fulldiveContext);
    }

    @Override
    public int getPages() {
        return 2;
    }

    @Override
    protected void onFinish() {
        if (!TextUtils.isEmpty(tag)) {
            getResourcesManager().setProperty(tag, true);
        }
        super.onFinish();
    }

    public void setTag(final String tag) {
        this.tag = tag;
    }

    @Override
    public void fillPage(FrameLayout frameLayout, int page) {
        switch (page) {
            case 0:
                fillPage1(frameLayout);
                break;
            case 1:
                fillPage2(frameLayout);
                break;
        }
    }

    private void fillPage1(FrameLayout frameLayout) {
        final float cx = frameLayout.getWidth() / 2f;
        final float cy = frameLayout.getHeight() / 2f;
        float top = VrConstants.SIZE_SMALL;
        ImageControl imageControl = new ImageControl();
        imageControl.setPivot(.5f, .5f);
        imageControl.setSize(1f, 1f);
        imageControl.setPosition(cx, top, 0f);
        imageControl.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.mediavr_tutorial_inform_icon));
        frameLayout.addControl(imageControl);

        top += VrConstants.SIZE_SMALL;

        addText(frameLayout, getString(R.string.mediavr_tutorial_activity_panel_title), 1.3f, cx, top, 0xffffffff);

        imageControl = new ImageControl();
        imageControl.setPivot(.5f, .5f);
        imageControl.setSize(5f, 5f);
        imageControl.setPosition(cx, cy + 0.5f, 0f);
        imageControl.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.mediavr_tutorial_activity_panel));
        frameLayout.addControl(imageControl);

        addText(frameLayout, getString(R.string.mediavr_tutorial_activity_panel_text), 0.9f, cx, cy + 3.5f, 0xffcccccc);
    }

    private void fillPage2(FrameLayout frameLayout) {
        final float cx = frameLayout.getWidth() / 2f;
        final float cy = frameLayout.getHeight() / 2f;
        float top = VrConstants.SIZE_SMALL;
        ImageControl imageControl = new ImageControl();
        imageControl.setPivot(.5f, .5f);
        imageControl.setSize(1f, 1f);
        imageControl.setPosition(cx, top, 0f);
        imageControl.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.mediavr_tutorial_inform_icon));
        frameLayout.addControl(imageControl);

        top += VrConstants.SIZE_SMALL;

        addText(frameLayout, getString(R.string.mediavr_tutorial_camera_title), 1.3f, cx, top, 0xffffffff);

        imageControl = new ImageControl();
        imageControl.setPivot(.5f, .5f);
        imageControl.setSize(5f, 5f);
        imageControl.setPosition(cx, cy + 0.5f, 0f);
        imageControl.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.mediavr_tutorial_camera));
        frameLayout.addControl(imageControl);

        addText(frameLayout, getString(R.string.mediavr_tutorial_camera_text), 0.9f, cx, cy + 3.5f, 0xffcccccc);
    }
}

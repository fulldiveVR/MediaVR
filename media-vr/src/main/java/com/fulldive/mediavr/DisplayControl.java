package com.fulldive.mediavr;

import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.support.annotation.NonNull;

import com.fulldive.basevr.components.SharedTexture;
import com.fulldive.basevr.controls.MeshControl;
import com.fulldive.basevr.framework.GLUtils;
import com.fulldive.basevr.framework.engine.GlEngine;
import com.fulldive.basevr.framework.engine.Mesh;
import com.fulldive.basevr.framework.engine.MeshBuilder;
import com.fulldive.basevr.framework.engine.TextureOESShader;
import com.fulldive.mediavr.MediaConstants;
import com.google.vr.sdk.base.Eye;

@SuppressWarnings("unused")
public class DisplayControl extends MeshControl {
    private Mesh meshLeft = new Mesh();
    private Mesh meshRight = new Mesh();
    private MeshBuilder meshBuilder = new MeshBuilder();
    private int mode = MediaConstants.MODE_FLAT;
    private SurfaceTexture surfaceTexture = null;
    private int videoType = MediaConstants.TYPE_VIDEO_2D;
    private SharedTexture sharedTexture = new SharedTexture();

    // for 360 video
    private int slices = 48;
    private int stacks = 48;

    @Override
    public void init() {
        super.init();
        meshBuilder.setUv(true).setPivotZ(1f);

        int textureId = GLUtils.generateTextureId();
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        sharedTexture.setTextureId(textureId);
        surfaceTexture = new SurfaceTexture(textureId);
        meshLeft.setShader(TextureOESShader.class);
        meshRight.setShader(TextureOESShader.class);
        meshLeft.setTextureType(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);
        meshRight.setTextureType(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);
        meshLeft.setSharedTexture(sharedTexture);
        meshRight.setSharedTexture(sharedTexture);
        setVisibilityChecking(false);
    }

    @Override
    public void dismiss() {
        if (sharedTexture != null) {
            sharedTexture.deleteTexture();
            sharedTexture = null;
        }
        meshLeft.setSharedTexture(null);
        meshRight.setSharedTexture(null);
        super.dismiss();
    }


    @Override
    public void updateSize() {
        super.updateSize();
        meshBuilder.setSize(getWidth(), getHeight(), getDepth());
        if (videoType == MediaConstants.TYPE_VIDEO_2D || videoType == MediaConstants.TYPE_VIDEO_3D) {
            meshBuilder.setIndices(false).setStacks(2).setSlices(2);
            updateMeshBuilderLeftTexture();
            meshBuilder.setMesh(meshLeft).buildRectangle();
            updateMeshBuilderRightTexture();
            meshBuilder.setMesh(meshRight).buildRectangle();
        } else if (videoType == MediaConstants.TYPE_VIDEO_180 || videoType == MediaConstants.TYPE_VIDEO_270 || videoType == MediaConstants.TYPE_VIDEO_360) {
            if (videoType == MediaConstants.TYPE_VIDEO_180)
                meshBuilder.setAngle(Math.PI);
            else if (videoType == MediaConstants.TYPE_VIDEO_270)
                meshBuilder.setAngle(Math.PI * 1.5);
            else meshBuilder.setAngle(0);

            meshBuilder.setStacks(stacks).setSlices(slices).setIndices(true);
            updateMeshBuilderLeftTexture();
            meshBuilder.setMesh(meshLeft).buildSphere();
            updateMeshBuilderRightTexture();
            meshBuilder.setMesh(meshRight).buildSphere();
        }
    }

    private void updateMeshBuilderLeftTexture() {
        switch (mode) {
            case MediaConstants.MODE_HORIZONTAL3D:
                meshBuilder.setTexture(0f, 0f, .5f, 1f);
                break;
            case MediaConstants.MODE_FLAT:
                meshBuilder.setTexture(0f, 0f, 1f, 1f);
                break;
            case MediaConstants.MODE_VERTICAL3D:
                meshBuilder.setTexture(0f, 0f, 1f, .5f);
                break;
        }
    }

    private void updateMeshBuilderRightTexture() {
        switch (mode) {
            case MediaConstants.MODE_HORIZONTAL3D:
                meshBuilder.setTexture(.5f, 0f, 1f, 1f);
                break;
            case MediaConstants.MODE_FLAT:
                meshBuilder.setTexture(0f, 0f, 1f, 1f);
                break;
            case MediaConstants.MODE_VERTICAL3D:
                meshBuilder.setTexture(0f, .5f, 1f, 1f);
                break;
        }
    }

    @Override
    public void onDraw(@NonNull GlEngine glEngine, @NonNull float[] view, @NonNull float[] headView, @NonNull float[] perspective, int eye) {
        setMesh(eye == Eye.Type.LEFT ? meshLeft : meshRight);
        super.onDraw(glEngine, view, headView, perspective, eye);
    }


    @Override
    public void onUpdate(final long timeMs) {
        super.onUpdate(timeMs);
        if (this.surfaceTexture != null && isVisible()) {
            surfaceTexture.updateTexImage();
        }
    }

    public SurfaceTexture getSurfaceTexture() {
        return surfaceTexture;
    }

    public void setMode(final int mode) {
        if (this.mode != mode) {
            this.mode = mode;
            invalidateSize();
        }
    }

    public int getMode() {
        return mode;
    }

    public int getVideoType() {
        return videoType;
    }

    public void setVideoType(final int videoType) {
        if (this.videoType != videoType) {
            final boolean changed = ((videoType == MediaConstants.TYPE_VIDEO_180) != (this.videoType == MediaConstants.TYPE_VIDEO_180)) ||
                    ((videoType == MediaConstants.TYPE_VIDEO_270) != (this.videoType == MediaConstants.TYPE_VIDEO_270)) ||
                    ((videoType == MediaConstants.TYPE_VIDEO_360) != (this.videoType == MediaConstants.TYPE_VIDEO_360));
            this.videoType = videoType;
            if (changed) {
                invalidateSize();
            }
        }
    }
}

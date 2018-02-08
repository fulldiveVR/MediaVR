package com.fulldive.mediavr;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.fulldive.basevr.controls.ButtonControl;
import com.fulldive.basevr.controls.Control;
import com.fulldive.basevr.controls.FrameLayout;
import com.fulldive.basevr.controls.ImageControl;
import com.fulldive.basevr.controls.OnControlClick;
import com.fulldive.basevr.controls.OnControlFocus;
import com.fulldive.basevr.controls.OnControlSelect;
import com.fulldive.basevr.controls.OnControlTouch;
import com.fulldive.basevr.controls.TextboxControl;
import com.fulldive.basevr.framework.FulldiveContext;
import com.fulldive.basevr.framework.ResourcesManager;
import com.fulldive.basevr.framework.TouchInfo;

import java.util.Arrays;

/**
 * Spiral Menu Control
 * Created by managerdark on 10.10.2015.
 */
//TODO: Make use size for fragment!!!! Important!
public class SpiralMenuControl extends FrameLayout implements OnControlFocus, OnControlTouch {
    private static final float MAX_LAG_THRESHOLD = 500;
    private TextboxControl emptyControl = null;
    private Control prevButton = null;
    private Control nextButton = null;
    private Control selectorControl = null;
    private Control selectedControl = null;
    private Control controls[] = null;
    private float radius = 12f;
    private float tiltThreshold = (float) (Math.PI / 15d);
    private float tiltButtonSpeed = 10f;
    private float tiltSpeed = 60f;

    private float itemsZ = 0f;
    private float targetItemsZ = 0f;
    private float selectorPadding = 2f;
    private float itemsShiftY = -7f;
    private float minZ = -10f;
    private float maxZ = 60f;
    private float minScale = 0.5f;
    private float itemDistanceZ = 10f;
    private float angleStart = (float) (Math.PI / 2d);
    private float angleShift = (float) (Math.PI * 2d);
    private int firstItem = 0;
    private int scrollDirect = 0;

    private float itemWidth = 8f;
    private float itemHeight = 8f;
    private float animationSpeed = 100f;
    private boolean enabled = false;
    private float touchX = 0f;

    private MenuAdapter adapter = null;
    private boolean datasetChanged = false;
    private OnControlSelect selectListener = null;
    private OnControlClick selectorListener = new OnControlClick() {
        @Override
        public void click(Control control) {
            SpiralMenuControl.this.click();
        }
    };

    public SpiralMenuControl(@NonNull final FulldiveContext fulldiveContext) {
        super(fulldiveContext);
        setFocusEventsMode(FOCUS_EVENTS_MODE_ALL);
        setVisibilityChecking(false);
    }

    public void setEmptyControl(final TextboxControl emptyControl) {
        if (this.emptyControl != emptyControl && this.emptyControl != null) {
            removeControl(this.emptyControl);
        }
        this.emptyControl = emptyControl;
        this.emptyControl.setVisible(false);
        addControl(emptyControl);
    }

    public void setSelectorControl(final Control selector) {
        selectorControl = selector;
        if (selectorControl != null) {
            selectorControl.setOnClickListener(selectorListener);
            selectorControl.setAutoClick(true);
            selectorControl.setForcedFocus(true);
        }
    }

    public void setNextButtonControl(final Control control) {
        nextButton = control;
        if (nextButton != null) {
            nextButton.setAutoClick(false);
        }
    }

    public void setPrevButtonControl(final Control control) {
        prevButton = control;
        if (prevButton != null) {
            prevButton.setAutoClick(false);
        }
    }

    public void setAdapter(final MenuAdapter adapter) {
        if (this.adapter != adapter) {
            removeAllControls();
            this.adapter = adapter;
            if (adapter == null) {
                controls = null;
            } else {
                controls = new Control[adapter.getCount()];
                Arrays.fill(controls, null);
            }
        }
    }

    @Override
    public void onUpdate(long timeMs) {
        super.onUpdate(timeMs);
        if (datasetChanged) {
            if (controls != null && controls.length > 0) {
                final int count = adapter.getCount();
                if (firstItem >= count) {
                    firstItem = count - 1;
                    itemsZ = 0f;
                    targetItemsZ = 0f;
                }

            } else {
                firstItem = 0;
                itemsZ = 0f;
                targetItemsZ = 0f;
            }
            calc(true);
            if (controls != null && controls.length > 0) {
                selectedControl = controls[firstItem];
                updateSelector();
            }
            datasetChanged = false;
        }

        if (enabled) {
            if (touchX <= -0.2f) {
                scrollDirect = 1;
            } else if (touchX >= 0.2f) {
                scrollDirect = -1;
            }

            float tiltValue = 0f;
            if (scrollDirect != 0) {
                tiltValue = scrollDirect > 0 ? tiltButtonSpeed : -tiltButtonSpeed;
                scrollDirect = 0;
            } else {
                final float eulerZ = parent.getEulerZ();
                if (Math.abs(eulerZ) > tiltThreshold) {
                    tiltValue = (eulerZ + (eulerZ >= 0 ? -tiltThreshold : tiltThreshold)) * tiltSpeed;
                }
            }
            if (tiltValue != 0f) {
                final float hd = itemDistanceZ / 2f;
                targetItemsZ += (Math.min(MAX_LAG_THRESHOLD, timeMs) / 1000f) * tiltValue;
                final int count = adapter.getCount();
                if (targetItemsZ < 0f) {
                    if (firstItem + 1 >= count) {
                        targetItemsZ = 0;
                    } else {
                        while (targetItemsZ < -hd) {
                            targetItemsZ += itemDistanceZ;
                            if (firstItem < count - 1) {
                                itemsZ += itemDistanceZ;
                                ++firstItem;
                            }
                        }
                    }
                }
                if (targetItemsZ > 0f) {
                    if (firstItem - 1 < 0) {
                        targetItemsZ = 0f;
                    } else {
                        while (targetItemsZ > hd) {
                            targetItemsZ -= itemDistanceZ;
                            if (firstItem > 0) {
                                itemsZ -= itemDistanceZ;
                                --firstItem;
                            }
                        }
                    }
                }
                if (controls != null && selectedControl != controls[firstItem]) {
                    selectedControl = controls[firstItem];
                    updateSelector();
                }
            }

            if (targetItemsZ != itemsZ) {
                final float len = Math.abs(itemsZ - targetItemsZ);
                final float diff = Math.min(((animationSpeed * Math.min(MAX_LAG_THRESHOLD, timeMs)) / 1000f), len);
                final float aligned_diff = (itemsZ < targetItemsZ) ? diff : -diff;
                itemsZ += aligned_diff;
                calc(false);
            }
            if (selectorControl != null) {
                if (selectedControl != null) {
                    selectorControl.setPosition(selectedControl.getX(), selectedControl.getY(), selectedControl.getZ() - 0.5f);
                    selectorControl.setVisible(true);
                } else {
                    selectorControl.setVisible(false);
                }
            }
        }
    }

    @Override
    public void init() {
        super.init();
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        if (selectorControl != null) {
            selectorControl.setPosition(0f, 0f, 0f);
            selectorControl.setPivot(0.5f, 0.5f);
            selectorControl.setSortIndex(10);
            selectorControl.setVisible(false);
            addControl(selectorControl);
            selectorControl.setOnClickListener(selectorListener);
            selectorControl.setOnTouchListener(this);
        }
        if (nextButton != null) {
            nextButton.setAutoClick(false);
            nextButton.setPivot(.5f, .5f);
            nextButton.setSize(3f, 3f);
            nextButton.setAlpha(0f);
            nextButton.setSortIndex(9);
            nextButton.setPosition(cx + 2f, cy - 2f, 2f);
            nextButton.setOnFocusListener(new OnControlFocus() {
                @Override
                public void onControlFocused(Control control) {
                    scrollDirect = -1;
                }

                @Override
                public void onControlUnfocused(Control control) {
                }
            });
            nextButton.setFocusEventsMode(FOCUS_EVENTS_MODE_ALL);
            nextButton.setForcedFocus(true);
            addControl(nextButton);
        }
        if (prevButton != null) {
            prevButton.setAutoClick(false);
            prevButton.setPivot(.5f, .5f);
            prevButton.setSize(3f, 3f);
            prevButton.setAlpha(0f);
            prevButton.setSortIndex(9);
            prevButton.setPosition(cx - 2f, cy - 2f, 2f);
            prevButton.setOnFocusListener(new OnControlFocus() {
                @Override
                public void onControlFocused(Control control) {
                    scrollDirect = 1;
                }

                @Override
                public void onControlUnfocused(Control control) {
                }
            });
            prevButton.setFocusEventsMode(FOCUS_EVENTS_MODE_ALL);
            prevButton.setForcedFocus(true);
            addControl(prevButton);
        }

        datasetChanged = true;
    }

    public void removeControls() {
        if (adapter == null) {
            return;
        }
        if (controls != null) {
            for (Control item : controls) {
                if (item != null) {
                    removeControl(item);
                }
            }
            controls = null;
        }
    }

    private void calc(final boolean reset) {
        if (reset) {
            removeControls();
        } else if (controls == null) {
            return;
        }
        final int count = adapter.getCount();
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;

        if (count == 0) {
            if (emptyControl != null) {
                emptyControl.setVisible(true);
                nextButton.setTargetAlpha(0f);
                prevButton.setTargetAlpha(0f);
            }
            return;
        }
        if (emptyControl != null) {
            emptyControl.setVisible(false);
        }
        if (nextButton != null) {
            nextButton.setTargetAlpha(1f);
        }
        if (prevButton != null) {
            prevButton.setTargetAlpha(1f);
        }

        if (!isVisible()) return;
        if (controls == null) {
            controls = new Control[count];
        }

        float item_z = -firstItem * itemDistanceZ + itemsZ;
        for (int i = 0; i < count; i++) {
            Control control = controls[i];
            if (item_z >= minZ && item_z <= maxZ) {
                if (control != null) {
                    control.setVisible(true);
                } else {
                    control = adapter.createControl();
                    adapter.bindControl(control, i);
                    control.setVisible(true);
                    control.setSize(itemWidth, itemHeight);
                    control.setPivot(0.5f, 0.5f);
                    control.setOnFocusListener(this);
                    control.setOnTouchListener(this);
                    controls[i] = control;
                    addControl(control);
                }

                float tmp = (item_z / maxZ);
                float angle = angleStart - tmp * angleShift;
                float scale = minScale + ((1f - minScale) * Math.max(0f, Math.min(1f, 1f - tmp)));
                final float ix = cx + (float) Math.cos(angle) * radius;
                final float iy = cy + (float) Math.sin(angle) * radius + itemsShiftY;
                control.setPosition(ix, iy, item_z);
                control.setAlpha(1f - Math.max(0f, Math.min(1f, tmp)));
                control.setScale(scale);
            } else if (control != null) {
                control.setVisible(false);
            }
            item_z += itemDistanceZ;
        }
    }

    public void notifyDatasetChanged() {
        datasetChanged = true;
    }

    @Override
    public void onControlFocused(Control control) {

    }

    @Override
    public void onControlUnfocused(Control control) {

    }

    public boolean isEnable() {
        return enabled;
    }

    @Override
    public void click() {
        if (enabled && clickListener != null && selectedControl != null) {
            clickListener.click(selectedControl);
        }
    }

    private void updateSelector() {
        if (selectedControl != null) {
            selectorControl.setSize(selectedControl.getWidth() + selectorPadding, selectedControl.getHeight() + selectorPadding);
            selectorControl.setScale(selectedControl.getScale());
            if (selectListener != null) {
                selectListener.selected(selectedControl);
            }
        }
        sortControls();
    }

    @Override
    public boolean onTouchEvent(@NonNull TouchInfo touchInfo, @Nullable Control target) {
        if (touchInfo.getSource() == TouchInfo.SOURCE_TOUCH) {
            if (touchInfo.getAction() == TouchInfo.ACTION_MOVE && (touchInfo.getTimestamp() - touchInfo.getInitialTimestamp() > 300)) {
                touchX = touchInfo.getX() - 0.5f;
            } else {
                touchX = 0f;
            }
            return true;
        }
        return super.onTouchEvent(touchInfo, target);
    }

    public void setOnItemSelectedListener(final OnControlSelect listener) {
        selectListener = listener;
    }

    public void setEnable(final boolean isEnable) {
        this.enabled = isEnable;
    }

    public static class Builder {
        final FulldiveContext fulldiveContext;

        public Builder(final FulldiveContext fulldiveContext) {
            this.fulldiveContext = fulldiveContext;
        }

        public SpiralMenuControl build(final int selectorId) {
            final ResourcesManager resourcesManager = fulldiveContext.getResourcesManager();
            final Resources resources = resourcesManager.getResources();
            final SpiralMenuControl control = new SpiralMenuControl(fulldiveContext);

            ImageControl selectorControl = new ImageControl();
            Bitmap bitmap = BitmapFactory.decodeResource(resources, selectorId);
            selectorControl.setImageBitmap(bitmap);
            bitmap.recycle();
            control.setSelectorControl(selectorControl);

            ButtonControl prevButton = new ButtonControl();
            prevButton.setNormalSprite(resourcesManager.getSprite("arrow_left_normal"));
            prevButton.setActiveSprite(resourcesManager.getSprite("arrow_left_pressed"));
            prevButton.setFocusedScale(1f);
            control.setPrevButtonControl(prevButton);

            ButtonControl nextButton = new ButtonControl();
            nextButton.setNormalSprite(resourcesManager.getSprite("arrow_right_normal"));
            nextButton.setActiveSprite(resourcesManager.getSprite("arrow_right_pressed"));
            nextButton.setFocusedScale(1f);
            control.setNextButtonControl(nextButton);

            return control;
        }
    }
}

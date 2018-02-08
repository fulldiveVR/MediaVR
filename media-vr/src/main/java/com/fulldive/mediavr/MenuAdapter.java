package com.fulldive.mediavr;

import com.fulldive.basevr.controls.Control;

/**
 * Created by managerdark on 10.10.2015.
 */
public interface MenuAdapter {
    int getCount();

    Control createControl();

    void bindControl(final Control control, final int position);

    Object getItem(final int position);
}

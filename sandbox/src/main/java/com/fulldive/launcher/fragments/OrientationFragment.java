package com.fulldive.launcher.fragments;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;

import com.fulldive.launcher.LauncherActivity;
import com.fulldive.launcher.R;

/**
 * Created by managerdark on 14.05.16.
 */
public class OrientationFragment extends BaseFragment {
    private static final String TAG = OrientationFragment.class.getSimpleName();

    @Override
    protected int getContentResId() {
        return R.layout.fragment_orientation;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        View skip = view.findViewById(R.id.startup_skip);
        if (skip != null) {
            skip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        LauncherActivity launcherActivity = (LauncherActivity) getActivity();
                        launcherActivity.onActionNext();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
        } else {
            try {
                LauncherActivity launcherActivity = (LauncherActivity) getActivity();
                launcherActivity.onActionNext();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        checkOrientation();
    }

    private void checkOrientation() {
        try {
            int orientation = this.getResources().getConfiguration().orientation;
            if (orientation != Configuration.ORIENTATION_PORTRAIT) {
                LauncherActivity launcherActivity = (LauncherActivity) getActivity();
                launcherActivity.onActionNext();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        checkOrientation();
    }
}

package com.fulldive.launcher.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;

import com.fulldive.launcher.Constants;
import com.fulldive.launcher.LauncherActivity;
import com.fulldive.launcher.R;

public class InstallShellFragment extends BaseFragment {
    private static final String TAG = InstallShellFragment.class.getSimpleName();

    @Override
    protected int getContentResId() {
        return R.layout.fragment_install_shell;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.proceed_button).setOnTouchListener(proceedToApp);
        view.findViewById(R.id.install_button).setOnTouchListener(proceedToInstall);
    }


    View.OnTouchListener proceedToApp = (view, motionEvent) -> {
        try {
            CheckBox cb = (CheckBox) getView().findViewById(R.id.dont_ask_checkbox);

            SharedPreferences.Editor editor = getContext().getSharedPreferences(Constants.perfName, 0).edit();
            editor.putBoolean(Constants.dontAskAgainKey, cb.isChecked());
            editor.apply();

            LauncherActivity launcherActivity = (LauncherActivity) getActivity();
            launcherActivity.onActionNext();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    };

    View.OnTouchListener proceedToInstall = (view, motionEvent) -> {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + Constants.shellPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + Constants.shellPackageName)));
        }
        try {
            getActivity().finish();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    };
}

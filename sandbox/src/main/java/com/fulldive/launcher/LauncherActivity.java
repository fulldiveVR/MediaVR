package com.fulldive.launcher;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.fulldive.launcher.fragments.OrientationFragment;
import com.fulldive.launcher.fragments.PermissionsFragment;

public class LauncherActivity extends RequestPermissionActivity {

    private static final String TAG = LauncherActivity.class.getSimpleName();
    private static final int STEP_PERMISSIONS = 3;
    private static final int STEP_ORIENTATION = 4;
    private int mStep = STEP_PERMISSIONS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_single_frame);
        if (savedInstanceState != null) {
            mWaitPermissionsAnswer = savedInstanceState.getBoolean("mWaitPermissionsAnswer", false);
            mStep = savedInstanceState.getInt("mStep", STEP_PERMISSIONS);
        }

        update();
    }

    public void startMainActivity() {
        try {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void showPermissionFragment() {
        replaceFragment(new PermissionsFragment(), false);
    }

    private void showOrientationFragment() {
        replaceFragment(new OrientationFragment(), false);
    }

    public void replaceFragment(Fragment fragment, boolean withBackStack) {
        replaceFragment(fragment, fragment.getClass().getCanonicalName(), withBackStack);
    }

    public void replaceFragment(Fragment fragment, String name, boolean withBackStack) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment, fragment);

        if (withBackStack) {
            transaction.addToBackStack(name);
        }
        transaction.commit();
    }

    public void onActionNext() {
        switch (mStep) {
            case STEP_PERMISSIONS:
                mStep = STEP_ORIENTATION;
                break;
            case STEP_ORIENTATION:
                startMainActivity();
                return;
        }
        update();
    }

    public void update() {
        switch (mStep) {
            case STEP_PERMISSIONS:
                showPermissionFragment();
                break;
            case STEP_ORIENTATION:
                showOrientationFragment();
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (outState != null) {
            outState.putBoolean("mWaitPermissionsAnswer", mWaitPermissionsAnswer);
            outState.putInt("mStep", mStep);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            mWaitPermissionsAnswer = savedInstanceState.getBoolean("mWaitPermissionsAnswer", false);
            mStep = savedInstanceState.getInt("mStep", STEP_PERMISSIONS);
        }
    }
}

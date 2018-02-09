package com.fulldive.launcher.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.fulldive.basevr.utils.FdLog;
import com.fulldive.launcher.LauncherActivity;
import com.fulldive.launcher.R;
import com.fulldive.launcher.RequestPermissionActivity;

/**
 * Created by managerdark on 14.05.16.
 */
public class PermissionsFragment extends BaseFragment {
    private static final String TAG = PermissionsFragment.class.getSimpleName();

    @Override
    protected int getContentResId() {
        return R.layout.fragment_permissions;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.request_button).setOnClickListener(v -> {
            try {
                RequestPermissionActivity requestPermissionActivity = (RequestPermissionActivity) getActivity();
                requestPermissionActivity.checkRequiredPermissions();
            } catch (Exception ex) {
                ex.printStackTrace();
                FdLog.e(TAG, ex.toString());
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        update();
    }

    private void update() {
        View view = getView();
        if (view != null) {
            TextView textView = (TextView) view.findViewById(R.id.message);
            try {
                RequestPermissionActivity requestPermissionActivity = (RequestPermissionActivity) getActivity();
                String permissions = requestPermissionActivity.getPermissionMessage();
                if (TextUtils.isEmpty(permissions)) {
                    if (requestPermissionActivity instanceof LauncherActivity) {
                        ((LauncherActivity)requestPermissionActivity).onActionNext();
                    } else {
                        requestPermissionActivity.finish();
                    }
                } else {
                    textView.setText(permissions);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                FdLog.e(TAG, ex.toString());
            }
        }
    }
}

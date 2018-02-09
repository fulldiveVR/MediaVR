package com.fulldive.launcher;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public abstract class RequestPermissionActivity extends AppCompatActivity {
    public static final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 2128506;
    public boolean mWaitPermissionsAnswer = false;

    public static int checkSelfPermission(@NonNull Context context, @NonNull String permission) {
        if (permission == null) {
            throw new IllegalArgumentException("permission is null");
        }

        return context.checkPermission(permission, android.os.Process.myPid(), Process.myUid());
    }

    public boolean shouldShowRequestPermissionRationale(@NonNull Activity activity,
                                                        @NonNull String permission) {
        if (Build.VERSION.SDK_INT >= 23) {
            return activity.shouldShowRequestPermissionRationale(permission);
        }
        return false;
    }

    private boolean addPermission(List<String> permissionsList, String permission) {
        if (checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            if (!shouldShowRequestPermissionRationale(this, permission)) {
                return false;
            }
        }
        return true;
    }

    public static void requestPermissions(final @NonNull Activity activity,
                                          final @NonNull String[] permissions, final int requestCode) {
        if (Build.VERSION.SDK_INT >= 23) {
            activity.requestPermissions(permissions, requestCode);
        } else if (activity instanceof OnRequestPermissionsResultCallback) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    final int[] grantResults = new int[permissions.length];

                    PackageManager packageManager = activity.getPackageManager();
                    String packageName = activity.getPackageName();

                    final int permissionCount = permissions.length;
                    for (int i = 0; i < permissionCount; i++) {
                        grantResults[i] = packageManager.checkPermission(
                                permissions[i], packageName);
                    }

                    ((OnRequestPermissionsResultCallback) activity).onRequestPermissionsResult(
                            requestCode, permissions, grantResults);
                }
            });
        }
    }

    public String getPermissionMessage() {
        final List<String> permissionsList = new ArrayList<>();
        PackageInfo info;
        try {
            info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_PERMISSIONS);
            if (info.requestedPermissions != null) {
                for (String permission : info.requestedPermissions) {
                    if (permission.equals("android.permission.READ_PHONE_STATE")) continue;
                    addPermission(permissionsList, permission);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!permissionsList.isEmpty()) {
            boolean hasPermissions = false;
            HashSet<String> hasSet = new HashSet<>();
            StringBuilder message = new StringBuilder(getString(R.string.message_permission_request));
            for (int i = 0; i < permissionsList.size(); i++) {
                String title = getPermissionTitle(permissionsList.get(i));
                if (hasSet.contains(title)) continue;
                hasPermissions = true;
                hasSet.add(title);
                if (i != 0)
                    message.append(", ");
                message.append(title);
            }
            return hasPermissions ? message.toString() : "";
        }
        return "";
    }

    public void checkRequiredPermissions() {
        if (mWaitPermissionsAnswer) return;
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_PERMISSIONS);
            final List<String> permissionsList = new ArrayList<>();

            if (info.requestedPermissions != null) {
                for (String permission : info.requestedPermissions) {
                    if (permission.equals("android.permission.READ_PHONE_STATE")) continue;
                    addPermission(permissionsList, permission);
                }
            }

            if (!permissionsList.isEmpty()) {
                mWaitPermissionsAnswer = true;
                requestPermissions(this, permissionsList.toArray(new String[permissionsList.size()]),
                        REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS:
                mWaitPermissionsAnswer = false;
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    private String getPermissionTitle(String permission) {
        switch (permission) {
            case "android.permission.VIBRATE":
                return getString(R.string.permission_vibrate);
            case "android.permission.INTERNET":
                return getString(R.string.permission_internet);
            case "android.permission.ACCESS_NETWORK_STATE":
                return getString(R.string.permission_network_state);
            case "android.permission.READ_EXTERNAL_STORAGE":
            case "android.permission.WRITE_EXTERNAL_STORAGE":
                return getString(R.string.permission_storage);
            case "android.permission.RECORD_AUDIO":
                return getString(R.string.permission_record_audio);
            case "android.permission.CAMERA":
                return getString(R.string.permission_camera);
            case "android.permission.WAKE_LOCK":
                return getString(R.string.permission_wake_lock);
        }

        int index;
        return !TextUtils.isEmpty(permission) && (index = permission.lastIndexOf(".")) > 0 ? permission.substring(index + 1) : "";
    }
}

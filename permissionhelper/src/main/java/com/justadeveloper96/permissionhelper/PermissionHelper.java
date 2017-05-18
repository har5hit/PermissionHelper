package com.justadeveloper96.permissionhelper;

import android.app.Activity;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by harshit on 10-03-2017.
 */


public class PermissionHelper {

    private static final String TAG = "Permission Manager";
    private int TYPE = 0;
    private final int ACTIVITY = 1;
    private final int FRAGMENT = 2;
    private final int FRAGMENTv4 = 3;

    public interface PermissionsListener {
        void onPermissionGranted(int request_code);
        void onPermissionRejectedManyTimes(@NonNull List<String> rejectedPerms, int request_code);
    }

    private WeakReference<Activity> activity_view;
    private WeakReference<Fragment> frag_view;
    private WeakReference<android.support.v4.app.Fragment> frag_v4_view;

    private PermissionsListener pListener;

    private List<String> deniedpermissions = new ArrayList<>();
    private List<String> granted = new ArrayList<>();

    public PermissionHelper(Activity view) {
        this.activity_view = new WeakReference<Activity>(view);
        TYPE = ACTIVITY;
    }

    public PermissionHelper(Fragment view) {
        this.frag_view = new WeakReference<Fragment>(view);
        TYPE = FRAGMENT;

    }

    public PermissionHelper(android.support.v4.app.Fragment view) {
        this.frag_v4_view = new WeakReference<android.support.v4.app.Fragment>(view);
        TYPE = FRAGMENTv4;
    }

    /**
     * Request permissions.
     *
     * @param permissions  -String Array of permissions to request, for eg: new String[]{PermissionManager.CAMERA} or multiple new String[]{PermissionManger.CAMERE,PermissionManager.CONTACTS}
     * @param request_code - Request code to check on callback.
     */
    public void requestPermission(@NonNull String[] permissions, int request_code) {
        deniedpermissions.clear();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isViewAttached()) {
            boolean allPermissionGranted = true;

            for (String permission : permissions) {
                if (getActivity().checkSelfPermission(permission) == PackageManager.PERMISSION_DENIED) {
                    allPermissionGranted = false;
                    deniedpermissions.add(permission);
                    Log.d(TAG, "denied " + permission);
                }
            }

            if (!allPermissionGranted) {
                switch (TYPE) {
                    case ACTIVITY:
                        getActivityView().requestPermissions(deniedpermissions.toArray(new String[deniedpermissions.size()]), request_code);
                        break;
                    case FRAGMENT:
                        getFrag_view().requestPermissions(deniedpermissions.toArray(new String[deniedpermissions.size()]), request_code);
                        break;
                    case FRAGMENTv4:
                        getFrag_v4_view().requestPermissions(deniedpermissions.toArray(new String[deniedpermissions.size()]), request_code);
                }
            } else {
                getListener().onPermissionGranted(request_code);
            }
        } else {
            getListener().onPermissionGranted(request_code);
        }
    }

    /***
     * After the permissions are requested, pass the results from Activity/fragments onRequestPermissionsResult to this function for processing
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @RequiresApi(Build.VERSION_CODES.M)
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (isViewAttached()) {
            String permission_name = "";
            boolean never_ask_again = false;
            granted.clear();

            for (String permission : deniedpermissions) {
                if (getActivity().checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
                    granted.add(permission);
                } else {
                    if (!getActivity().shouldShowRequestPermissionRationale(permission)) {
                        never_ask_again = true;
                    }
                    permission_name += "," + PermissionHelper.getNameFromPermission(permission);
                }
            }

            deniedpermissions.removeAll(granted);

            if (deniedpermissions.size() > 0) {
                permission_name = permission_name.substring(1);
                if (!never_ask_again) {
                    getRequestAgainAlertDialog(getActivity(), permission_name,requestCode);
                } else {
                    goToSettingsAlertDialog(getActivity(), permission_name,requestCode);
                }
            } else {
                getListener().onPermissionGranted(requestCode);
            }
        }
    }

    private AlertDialog goToSettingsAlertDialog(final Activity view, String permission_name, final int request_code) {
        return new AlertDialog.Builder(view).setTitle("Permission Required").setMessage("We need " + permission_name + " permissions")
                .setPositiveButton("Go to Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.addCategory(Intent.CATEGORY_DEFAULT);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        intent.setData(Uri.parse("package:" + view.getPackageName()));
                        view.startActivity(intent);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        getListener().onPermissionRejectedManyTimes(deniedpermissions, request_code);
                    }
                }).show();
    }

    private AlertDialog getRequestAgainAlertDialog(Activity view, String permission_name, final int request_code) {
        return new AlertDialog.Builder(view).setTitle("Permission Required")
                .setMessage("We need " + permission_name + " permissions")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        requestPermission(deniedpermissions.toArray(new String[deniedpermissions.size()]), request_code);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        getListener().onPermissionRejectedManyTimes(deniedpermissions, request_code);
                    }
                }).show();
    }


    private boolean isViewAttached() {
        switch (TYPE) {
            case ACTIVITY:
                return activity_view.get() != null;
            case FRAGMENT:
                return frag_view.get() != null;
            case FRAGMENTv4:
                return frag_v4_view.get() != null;
        }
        return false;
    }

    private Activity getActivityView() {
        return activity_view.get();
    }

    private Fragment getFrag_view() {
        return frag_view.get();
    }

    private android.support.v4.app.Fragment getFrag_v4_view() {
        return frag_v4_view.get();
    }

    private Activity getActivity() {
        switch (TYPE) {
            case ACTIVITY:
                return getActivityView();
            case FRAGMENT:
                return getFrag_view().getActivity();
            case FRAGMENTv4:
                return getFrag_v4_view().getActivity();
        }
        return null;
    }

    private PermissionsListener getListener() {
        return pListener;
    }

    public static String getNameFromPermission(String permission) {

        String key = permission.toLowerCase();
        if (key.contains("storage")) {
            return "Storage";
        }
        if (key.contains("camera")) {
            return "Camera";
        }
        if (key.contains("phone") || key.contains("call")) {
            return "Call";
        }

        String[] split = permission.split("\\.");
        return split[split.length - 1];
    }

    public PermissionHelper setListener(PermissionsListener pListener) {
        this.pListener = pListener;
        return this;
    }

    public void onDestroy() {
        pListener = null;
        activity_view = null;
        frag_view = null;
        frag_v4_view = null;
    }

}
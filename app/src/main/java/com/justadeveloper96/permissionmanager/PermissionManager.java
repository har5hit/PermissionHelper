package com.justadeveloper96.permissionmanager;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by harshit on 10-03-2017.
 */


public class PermissionManager {

    public static final String TAG="Permission Manager";

    interface PermissionsListener
    {
         void onPermissionGranted();
         void onPermissionRejectedManyTimes(List<String> rejectedPerms);
    }

    WeakReference<AppCompatActivity> view;

    PermissionsListener pListener;

    boolean allPermissionGranted;
    List<String> deniedpermissions;
    List<String> granted;

    final int REQUEST_CODE=100;

    public PermissionManager(AppCompatActivity view) {
        this.view = new WeakReference<AppCompatActivity>(view);
        deniedpermissions=new ArrayList<>();
        granted=new ArrayList<>();
    }

    /**
     * Request permissions.
     * @param permissions -String Array of permissions to request, for eg: new String[]{PermissionManager.CAMERA} or multiple new String[]{PermissionManger.CAMERE,PermissionManager.CONTACTS}
     *
     */
    protected void requestPermission(String[] permissions) {
        deniedpermissions.clear();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isViewAttached()) {
            allPermissionGranted = true;
            for (String permission : permissions)
                if (getView().checkSelfPermission(permission) == PackageManager.PERMISSION_DENIED) {
                    allPermissionGranted = false;
                    deniedpermissions.add(permission);
                    Log.d(TAG, "denied " + permission);
                }

            if (!allPermissionGranted) {
                getView().requestPermissions(deniedpermissions.toArray(new String[deniedpermissions.size()]), REQUEST_CODE);
            } else {
                getListener().onPermissionGranted();
            }

            return;
        }
        getListener().onPermissionGranted();
    }

    protected void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE && isViewAttached() && Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
            String permission_name="";
            boolean never_ask_again=false;
            granted.clear();

            for (String permission : deniedpermissions) {
                if (getView().checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
                    granted.add(permission);
                }else
                {
                    if(!getView().shouldShowRequestPermissionRationale(permission))
                    {
                        never_ask_again=true;
                    }
                    permission_name+=","+PermissionManager.getNameFromPermission(permission);
                }
            }

            deniedpermissions.removeAll(granted);

            if (deniedpermissions.size() > 0) {
                permission_name=permission_name.substring(1);
                if (!never_ask_again) {
                    new AlertDialog.Builder(getView()).setTitle("Permission Required").setMessage("We need permissions to access " + permission_name + " permissions")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    requestPermission(deniedpermissions.toArray(new String[deniedpermissions.size()]));
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    getListener().onPermissionRejectedManyTimes(deniedpermissions);
                                }
                            }).show();
                } else {
                    new AlertDialog.Builder(getView()).setTitle("Permission Required").setMessage("We need permissions to access " + permission_name + " permission")
                            .setPositiveButton("Go to Settings", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    final Intent intent = new Intent();
                                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                                    intent.setData(Uri.parse("package:" + getView().getPackageName()));
                                    getView().startActivity(intent);
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    getListener().onPermissionRejectedManyTimes(deniedpermissions);
                                }
                            }).show();
                }

            }
            else
            {
                getListener().onPermissionGranted();
            }
        }
    }


    private boolean isViewAttached() {
        return view.get()!=null?true:false;
    }

    private AppCompatActivity getView() {
        return view.get();
    }

    private PermissionsListener getListener() {
        return pListener;
    }

    public static String getNameFromPermission(String permission)
    {
        String[] split=permission.split("\\.");
        return split[split.length-1];
    }

    protected PermissionManager setListener(PermissionsListener pListener)
    {
        this.pListener=pListener;
        return this;
    }

    protected void onDestroy()
    {
        pListener=null;
        view=null;
    }
}
package com.justadeveloper96.permissionmanager;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by harshit on 10-03-2017.
 */

public class PermissionManager {

    interface PermissionsListener
    {
        public void onPermissionGranted();
        public void onPermissionRejectedManyTimes();
    }

    WeakReference<PermissionsListener> view;
    boolean allPermissionGranted;
    List<String> deniedpermissions;

    final int REQUEST_CODE=100;

    public PermissionManager(PermissionsListener view) {
        this.view = new WeakReference<PermissionsListener>(view);
        deniedpermissions=new ArrayList<>();
    }

    public void requestPermission(String[] permissions)
    {
        if(Build.VERSION.SDK_INT< Build.VERSION_CODES.M)
        {
            return;
        }
        if(isViewAttached())
        {
            allPermissionGranted=true;
            for (String permission:permissions)
                if(getView().checkSelfPermission(permission)== PackageManager.PERMISSION_DENIED)
                {
                    allPermissionGranted=false;
                    getView().requestPermissions(permissions,REQUEST_CODE);
                    deniedpermissions.add(permission);
                }
        }

    }

    @TargetApi(Build.VERSION_CODES.M)
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==REQUEST_CODE && isViewAttached())
        {
           /* if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                view.get().onPermissionGranted();
                return;
            }*/

            for (String permission:deniedpermissions) {
                if (getView().checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
                    deniedpermissions.remove(permission);
                    continue;
                }

                if (getView().shouldShowRequestPermissionRationale(permission)) {
                    new AlertDialog.Builder(getView()).setTitle("Permission Required").setMessage("need " + permission + " permission")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    requestPermission(deniedpermissions.toArray(new String[deniedpermissions.size()]));
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Toast.makeText(getView(), "He denied again", Toast.LENGTH_SHORT).show();
                                }
                            }).show();
                } else {
                    new AlertDialog.Builder(getView()).setTitle("Permission Required").setMessage("need " + permission + " permission")
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
                                    Toast.makeText(getView(), "He denied again", Toast.LENGTH_SHORT).show();
                                }
                            }).show();
                }
            }

            if (deniedpermissions.size()<1)
            {
                view.get().onPermissionGranted();
                view=null;
            }
        }
    }

    public boolean isViewAttached() {
        return view.get()!=null?true:false;
    }

    public AppCompatActivity getView() {
        return (AppCompatActivity)view.get();
    }
}

package com.justadeveloper96.permissionmanager;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by harshit on 10-03-2017.
 */


public class PermissionManager {

    public static final String TAG="Permission Manager";
    private  int TYPE=0;
    private  final int ACTIVITY=1;
    private  final int FRAGMENT=2;
    private  final int FRAGMENTv4=3;

    interface PermissionsListener
    {
        void onPermissionGranted();
        void onPermissionRejectedManyTimes(List<String> rejectedPerms);
    }

    private WeakReference<Activity> activity_view;
    private WeakReference<Fragment> frag_view;
    private WeakReference<android.support.v4.app.Fragment> frag_v4_view;

    private PermissionsListener pListener;

    private boolean allPermissionGranted;

    private List<String> deniedpermissions=new ArrayList<>();
    private List<String> granted=new ArrayList<>();

    final int REQUEST_CODE=100;

    public PermissionManager(Activity view) {
        this.activity_view = new WeakReference<Activity>(view);
        TYPE=ACTIVITY;
    }

    public PermissionManager(Fragment view) {
        this.frag_view = new WeakReference<Fragment>(view);
        TYPE=FRAGMENT;

    }

    public PermissionManager(android.support.v4.app.Fragment view) {
        this.frag_v4_view = new WeakReference<android.support.v4.app.Fragment>(view);
        TYPE=FRAGMENTv4;

    }

    /**
     * Request permissions.
     * @param permissions -String Array of permissions to request, for eg: new String[]{PermissionManager.CAMERA} or multiple new String[]{PermissionManger.CAMERE,PermissionManager.CONTACTS}
     *
     */
    public void requestPermission(String[] permissions) {

        deniedpermissions.clear();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isViewAttached()) {
            allPermissionGranted = true;

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
                        getActivityView().requestPermissions(deniedpermissions.toArray(new String[deniedpermissions.size()]), REQUEST_CODE);
                        break;
                    case FRAGMENT:
                        getFrag_view().requestPermissions(deniedpermissions.toArray(new String[deniedpermissions.size()]), REQUEST_CODE);
                        break;
                    case FRAGMENTv4:
                        getFrag_v4_view().requestPermissions(deniedpermissions.toArray(new String[deniedpermissions.size()]), REQUEST_CODE);
                }
            } else
            {
                getListener().onPermissionGranted();
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE && isViewAttached() && Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
            String permission_name="";
            boolean never_ask_again=false;
            granted.clear();

            for (String permission : deniedpermissions) {
                if (getActivity().checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
                    granted.add(permission);
                }else
                {
                    if(!getActivity().shouldShowRequestPermissionRationale(permission))
                    {
                        never_ask_again=true;
                    }
                    permission_name+=","+PermissionManager.getNameFromPermission(permission);
                }
            }

            deniedpermissions.removeAll(granted);

            if (deniedpermissions.size() > 0) {
                permission_name=permission_name.substring(1);
                if (!never_ask_again){
                    getRequestAgainAlertDialog(getActivity(),permission_name);
                }else {
                    goToSettingsAlertDialog(getActivity(), permission_name);
                }
            }
            else
            {
                getListener().onPermissionGranted();
            }
        }
    }

    private AlertDialog goToSettingsAlertDialog(final Activity view, String permission_name) {
        return new AlertDialog.Builder(view).setTitle("Permission Required").setMessage("We need " + permission_name +" permissions")
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
                        getListener().onPermissionRejectedManyTimes(deniedpermissions);
                    }
                }).show();
    }

    private AlertDialog getRequestAgainAlertDialog(Activity view, String permission_name) {
        return new AlertDialog.Builder(view).setTitle("Permission Required")
                .setMessage("We need " + permission_name +" permissions")
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
    }




    private boolean isViewAttached() {
        switch (TYPE)
        {
            case ACTIVITY:
                return activity_view.get()!=null;
            case FRAGMENT:
                return frag_view.get()!=null;
            case FRAGMENTv4:
                return frag_v4_view.get()!=null;
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
        switch (TYPE)
        {
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

    public static String getNameFromPermission(String permission)
    {
        String[] split=permission.split("\\.");
        return split[split.length-1];
    }

    public PermissionManager setListener(PermissionsListener pListener)
    {
        this.pListener=pListener;
        return this;
    }

    public void onDestroy()
    {
        pListener=null;
        activity_view=null;
        frag_view=null;
        frag_v4_view=null;
    }


    @TargetApi(Build.VERSION_CODES.M)
    public static List<PermissionModel> getAllDeniedPermissions(Context ctx)
    {
        List<PermissionModel> temp=new ArrayList<>();
        try {
            PackageInfo info = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), PackageManager.GET_PERMISSIONS);
            if (info.requestedPermissions != null) {
                for (String p : info.requestedPermissions) {
                    if (ctx.checkSelfPermission(p)==PackageManager.PERMISSION_DENIED)
                        temp.add(new PermissionModel(p));
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return temp;
    }

}
package com.justadeveloper96.permissionmanager;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.FeatureInfo;
import android.content.pm.InstrumentationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.database.Observable;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableField;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.nfc.Tag;
import android.os.UserHandle;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.annotation.XmlRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.Toast;

import com.justadeveloper96.permissionmanager.databinding.ActivityMainBinding;

import java.security.Permission;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity{

    List<String> needed_permissions;
    public PermissionModel model;
    PermissionManager permissionManager;
    ObservableField<Integer> permissioncount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        permissioncount=new ObservableField<>(0);

        needed_permissions=new ArrayList<>();

        binding.setCount(permissioncount);

        List<PermissionModel> manifest_permissions=new ArrayList<>();

        try {
            PackageInfo info = getPackageManager().getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS);
            if (info.requestedPermissions != null) {
                for (String p : info.requestedPermissions) {
                    Log.d(PermissionManager.TAG,"manifest permissions "+p);
                    manifest_permissions.add(new PermissionModel(PermissionManager.getNameFromPermission(p),p));
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        final ArrayAdapter<PermissionModel> arrayAdapter=new ArrayAdapter<PermissionModel>(this,android.R.layout.simple_list_item_checked,manifest_permissions);
        binding.listview.setAdapter(arrayAdapter);
        binding.listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(PermissionManager.TAG,"List item clicked "+arrayAdapter.getItem(i).getName());
                if(arrayAdapter.getItem(i).isSelected())
                {
                    arrayAdapter.getItem(i).setSelected(false);
                    ((CheckedTextView)view).setChecked(false);
                    permissioncount.set(permissioncount.get()-1);
                    return;
                }
                arrayAdapter.getItem(i).setSelected(true);
                ((CheckedTextView)view).setChecked(true);
                permissioncount.set(permissioncount.get()+1);
            }
        });


        permissionManager=new PermissionManager(this).setListener(new PermissionManager.PermissionsListener() {
            @Override
            public void onPermissionGranted() {
                Toast.makeText(MainActivity.this,"Granted all",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionRejectedManyTimes(List<String> rejectedPerms) {
                String s="";
                for(String perms:rejectedPerms)
                {
                    s+=","+PermissionManager.getNameFromPermission(perms);
                }
                Toast.makeText(MainActivity.this,"Rejected "+s.substring(1),Toast.LENGTH_SHORT).show();
            }
        });

        binding.get.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                needed_permissions.clear();
                for (int i=0;i<arrayAdapter.getCount();i++)
                {
                    PermissionModel item=arrayAdapter.getItem(i);
                    if(item.isSelected())
                    {
                        needed_permissions.add(item.getValue());
                        Log.d(PermissionManager.TAG,"Going to Request"+item.getName());
                    }
                }
                permissionManager.requestPermission(needed_permissions.toArray(new String[needed_permissions.size()]));
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionManager.onRequestPermissionsResult(requestCode,permissions,grantResults);
    }
}

package com.justadeveloper96.permissionmanager;

import android.app.Fragment;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableField;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import com.justadeveloper96.permissionmanager.databinding.ActivityMainBinding;

/**
 * Created by harshit on 10-03-2017.
 */


public class MainActivity extends AppCompatActivity{

    PermissionManager permissionManager;
    ObservableField<Integer> permissioncount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        permissioncount=new ObservableField<>(0);
        binding.setCount(permissioncount);

        final List<String> needed_permissions=new ArrayList<>();
        final List<PermissionModel> manifest_permissions=new ArrayList<>();

        /**
         * Get all app permissions from Manifest
         */
        manifest_permissions.addAll(PermissionManager.getAllDeniedPermissions(this));

        final ArrayAdapter<PermissionModel> arrayAdapter=new ArrayAdapter<PermissionModel>(this,android.R.layout.simple_list_item_checked,manifest_permissions);
        binding.listview.setAdapter(arrayAdapter);
        binding.listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
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


        Fragment m=new Fragment();
        android.support.v4.app.Fragment n=new android.support.v4.app.Fragment();
        FragmentActivity c=new FragmentActivity();
        DialogFragment d=new DialogFragment();


        /**
         * Initialize permission Manager and set Listener.
         */
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

        /**
         * pass the permission results to the PermissionManager for processing.
         */
        permissionManager.onRequestPermissionsResult(requestCode,permissions,grantResults);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        permissionManager.onDestroy();
    }
}

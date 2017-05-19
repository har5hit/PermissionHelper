# PermissionHelper
Android Permission Helper Library for easy permission requests.
This will handle most of your permission handling job and simplify it by giving only 2 callbacks to the listener, success or failure.
Included a Rationale to show when user denies permission.

## Getting Started

Just create PermissionHelper object by passing the Activity/Fragment and a listener for callbacks

```
PermissionHelper permissionHelper;

@Override
protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	.
	.
	permissionHelper=new PermissionHelper(this).setListener(new PermissionHelper.PermissionsListener() {
		@Override
		public void onPermissionGranted(int request_code) {

			//if user grants permission.

		}

		@Override
		public void onPermissionRejectedManyTimes(List<String> rejectedPerms,int request_code) {

			//if user keeps on denying request
		}
	});
```

##Requesting Permissions

As we did in normal runtime permissions we will send a String array with permissions we want to request, we can send single or multiple permissions with it,
This will request the permissions with the specified request code..

```
	String[] needed_permissions=new String[]{Manifest.permission.CAMERA};

	-or-

	String[] needed_permissions=new String[]{Manifest.permission.CAMERA, Manifest.permission.CALL_PHONE};


	permissionHelper.requestPermission(needed_permissions,100);

```

The android system will give the results of the request and we have to pass that to the PermissionHelper to process.

```
@Override
public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

	/**
    * pass the permission results to the PermissionHelper for processing.
    */
    permissionHelper.onRequestPermissionsResult(requestCode,permissions,grantResults);

}
```

### Response

The PermissionHelper will request permission, if denied, will show rationale, if never ask again is checked, will show a dialog to
grant permissions in settings. If the user agrees to grant permission or denies again and again, the PermissionHelper will give the callback to the listener methods.

```
	@Override
	public void onPermissionGranted(int request_code) {

		//if user grants permission.

	}

	@Override
	public void onPermissionRejectedManyTimes(List<String> rejectedPerms,int request_code) {

		//if user keeps on denying request

	}

```


### Closing

After we are done with our permission requesting process and we no longer need the PermissionHelper we can call onDestroy() to free it.

```
	permissionHelper.onDestroy();
```


## Demo

The project is a demo with a list view of permissions and using PermissionHelper for handling request.
Clone the source, compile and run the project for trial.


## License

This project is licensed under the Apache 2.0 License - see the [LICENSE](LICENSE) file for details

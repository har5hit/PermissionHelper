# PermissionManager
Android Permission Manager Library for easy permission requests.

# Project Title

One Paragraph of project description goes here

## Getting Started

Just create PermissionManager object by passing the Activity and a listener for callbacks

```
PermissionManager permissionManager;

@Override
protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	.
	.
	permissionManager=new PermissionManager(this).setListener(new PermissionManager.PermissionsListener() {
		@Override
		public void onPermissionGranted() {

			//if user grants permission.

		}

		@Override
		public void onPermissionRejectedManyTimes(List<String> rejectedPerms) {

			//if user keeps on denying request
		}
	});

	```

	##Requesting Permissions

	As we did in normal runtime permissions we will send a String array with permissions we want to request, we can send single or multiple permissions with it,
	This will request the permissions.

	```
	String[] needed_permissions=new String[]{Manifest.permission.CAMERA};
	-or-
	String[] needed_permissions=new String[]{Manifest.permission.CAMERA, Manifest.permission.CALL_PHONE};


	permissionManager.requestPermission(needed_permissions);


	```

	The android system will give the results of the request and we have to pass that to the PermissionManager to process.

	```
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		/**
						* pass the permission results to the PermissionManager for processing.
						*/
		permissionManager.onRequestPermissionsResult(requestCode,permissions,grantResults);
	}

	```



	### Response

	The PermissionManager will request permission, if denied, will show rationale, if never ask again is checked, will show a dialog to
	grant permissions in settings. If the user agrees to grant permission or denies again and again, the PermissionManager will give the callback to the listener methods.

	```
	@Override
	public void onPermissionGranted() {

		//if user grants permission.

	}

	@Override
	public void onPermissionRejectedManyTimes(List<String> rejectedPerms) {

		//if user keeps on denying request

	}

	```


	### Closing

	After we are done with our permission requesting process and we no longer need the PermissionManager we can call onDestroy() to free it.

	```

	permissionManager.onDestroy();

	```


	## Demo

	The project is a demo with a list view of permissions and using PermissionManager for handling request.
	Clone the source, compile and run the project for trial.


	## License

	This project is licensed under the Apache 2.0 License - see the [LICENSE](LICENSE) file for details

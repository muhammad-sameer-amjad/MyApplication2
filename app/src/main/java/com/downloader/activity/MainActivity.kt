package com.downloader.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.downloader.R
import com.downloader.common.Common.Companion.READ_EXTERNAL_STORAGE_REQUEST
import com.downloader.common.DialogUtil
import com.downloader.common.PermissionUtil
import com.downloader.interfaces.DialogInterface
import com.downloader.service.DownloadService
import com.google.android.material.snackbar.Snackbar


class MainActivity : AppCompatActivity(), DialogInterface {
    private lateinit var ctx: Context
    private lateinit var dialog: DialogUtil
    private var isNotificationAllowed: Boolean = true
    private val CODE_DRAW_OVER_OTHER_APP_PERMISSION = 2084
    private val TAG="MAIN_TAG"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
    }

    private fun init() {
        ctx = applicationContext
        dialog = DialogUtil(
            this,
            getString(R.string.storage_permission_title),
            getString(R.string.storage_permission_detail),
            getString(R.string.allow),
            getString(R.string.deny),
            this
        )
        dialog.init()
        if (!PermissionUtil.haveStoragePermission(ctx)) {
            dialog.showDialog()
        }else{
            checkNotificationPermissionForTiramisu()
        }

    }

    fun checkNotificationPermissionForTiramisu() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            when {

                ContextCompat.checkSelfPermission(
                    ctx, Manifest.permission.POST_NOTIFICATIONS
                )
                        == PackageManager.PERMISSION_GRANTED -> {
                    // You can use the API that requires the permission.
                    Log.d(TAG, "Notification PERMISSION GRANTED")
                    isNotificationAllowed = true
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(
                            this
                        )
                    ) {

                        //If the draw over permission is not available open the settings screen
                        //to grant the permission.
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:$packageName")
                        )
                        startActivityForResult(intent, CODE_DRAW_OVER_OTHER_APP_PERMISSION)
                    } else {
                        startForeground()
                    }


                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {

                        Snackbar.make(
                            findViewById(android.R.id.content),
                            "Notification blocked",
                            Snackbar.LENGTH_LONG
                        ).setAction("Settings") {
                            // Responds to click on the action
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            val uri: Uri =
                                Uri.fromParts("package", packageName, null)
                            intent.data = uri
                            startActivity(intent)
                        }.show()

                }
                else -> {
                    // The registered ActivityResultCallback gets the result of this request
                    requestPermissionLauncher.launch(
                        Manifest.permission.POST_NOTIFICATIONS
                    )
                }
            }

        }
    }

    override fun positiveDialogButtonPressed() {
        requestPermission()

    }

    override fun negativeDialogButtonPressed() {

    }

    private fun requestPermission() {
        if (!PermissionUtil.haveStoragePermission(this)) {
            val permissions = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(
                    this,
                    permissions_33,
                    READ_EXTERNAL_STORAGE_REQUEST
                )
            } else {
                ActivityCompat.requestPermissions(this, permissions, READ_EXTERNAL_STORAGE_REQUEST)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    val permissions_33 = arrayOf(
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.READ_MEDIA_AUDIO,
        Manifest.permission.READ_MEDIA_VIDEO
    )

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            READ_EXTERNAL_STORAGE_REQUEST -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (grantResults.isNotEmpty() && grantResults.size == 3 && grantResults[0] == PermissionChecker.PERMISSION_GRANTED
                        && grantResults[1] == PermissionChecker.PERMISSION_GRANTED && grantResults[2] == PermissionChecker.PERMISSION_GRANTED
                    ) {
                        //permission granted
                        checkNotificationPermissionForTiramisu()
                    } else {
                        //permission not granted
                    }

                } else {
                    // If request is cancelled, the result arrays are empty.
                    if (grantResults.isNotEmpty() && grantResults[0] == PermissionChecker.PERMISSION_GRANTED) {
                        //permission granted

                    } else {
                        // If we weren't granted the permission, check to see if we should show
                        // rationale for the permission.
                        val showRationale =
                            ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                Manifest.permission.READ_EXTERNAL_STORAGE
                            )

                        /**
                         * If we should show the rationale for requesting storage permission, then
                         * we'll show [ActivityMainBinding.permissionRationaleView] which does this.
                         *
                         * If `showRationale` is false, this means the user has not only denied
                         * the permission, but they've clicked "Don't ask again". In this case
                         * we send the user to the settings page for the app so they can grant
                         * the permission (Yay!) or uninstall the app.
                         */
                        if (showRationale) {
                            //permission not granted
                        } else {
                            goToSettings()
                        }
                    }
                    return
                }

            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {

            isNotificationAllowed = true
            startForeground()

        } else {

            isNotificationAllowed = false
        }
    }

    private fun goToSettings() {
        Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:$packageName")
        ).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }.also { intent ->
            startActivity(intent)
        }
    }

    fun startForeground(){
        val intent = Intent(this, DownloadService::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CODE_DRAW_OVER_OTHER_APP_PERMISSION) {

            //Check if the permission is granted or not.
            if (resultCode == RESULT_OK) {
                startForeground()
            } else { //Permission is not available
                Toast.makeText(this,
                    "Draw over other app permission not available. Closing the application",
                    Toast.LENGTH_SHORT).show();

                finish();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
package com.downloader.common

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker

class PermissionUtil {
    companion object{
        fun haveStoragePermission(ctx: Context) =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    ctx,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) == PermissionChecker.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(
                            ctx,
                            Manifest.permission.READ_MEDIA_AUDIO
                        ) == PermissionChecker.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(
                            ctx,
                            Manifest.permission.READ_MEDIA_VIDEO
                        ) == PermissionChecker.PERMISSION_GRANTED
            } else {
                ContextCompat.checkSelfPermission(
                    ctx,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PermissionChecker.PERMISSION_GRANTED
            }
    }
}
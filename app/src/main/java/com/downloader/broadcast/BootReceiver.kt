package com.downloader.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.downloader.service.DownloadService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BOOT_LOG","Device rebooted and app is going to start app service")
            startForeground(context)
        }
    }

    fun startForeground(ctx:Context){
        val intent = Intent(ctx, DownloadService::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ctx.startForegroundService(intent)
        } else {
            ctx.startService(intent)
        }
    }
}
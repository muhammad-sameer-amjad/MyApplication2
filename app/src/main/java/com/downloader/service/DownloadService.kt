package com.downloader.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.view.View.GONE
import android.view.View.OnTouchListener
import android.view.View.VISIBLE
import android.widget.ImageButton
import android.widget.ImageView
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.downloader.R
import com.downloader.activity.MainActivity
import com.downloader.adapter.FileAdapter
import java.io.File


class DownloadService : Service() {
    private lateinit var mWindowManager: WindowManager
    private lateinit var popupView: View
    private val TAG = "DOWNLOAD_SERVICE_TAG"
    private lateinit var closeImageBtn:ImageButton
    private lateinit var pop_upIV:ImageView
    private lateinit var recyclerView:RecyclerView

    private val mFiles = mutableListOf<File>()

    override fun onCreate() {
        super.onCreate()
        init()

    }

    @SuppressLint("ClickableViewAccessibility")
    fun init() {
        popupView = LayoutInflater.from(this).inflate(R.layout.layou_pop_up_head, null);
        closeImageBtn = popupView.findViewById(R.id.close_btn)
        pop_upIV = popupView.findViewById(R.id.pop_upIV)
        recyclerView = popupView.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        mFiles.addAll(getDownloadedFiles())
        Log.d(TAG,"Files list: "+mFiles.toString())
        val adapter = FileAdapter(mFiles)
        recyclerView.adapter = adapter

        /*var params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )*/
        var params = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )
        } else {
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )
        }

        params.gravity = Gravity.TOP or Gravity.LEFT
        params.x = 0
        params.y = 100

        //Add the view to the window
        mWindowManager = getSystemService(WINDOW_SERVICE) as WindowManager;
        mWindowManager.addView(popupView, params);

        /*closeImageBtn.setOnClickListener(){

        }*/



        pop_upIV.setOnTouchListener(object : OnTouchListener {
            private var lastAction = 0
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f
            override fun onTouch(v: View?, event: MotionEvent): Boolean {

                when (event.getAction()) {
                    MotionEvent.ACTION_DOWN -> {
                        if(recyclerView.visibility == VISIBLE){
                            recyclerView.visibility = GONE
                        }else{
                            recyclerView.visibility = VISIBLE
                        }

                        //remember the initial position.
                        initialX = params.x
                        initialY = params.y

                        //get the touch location
                        initialTouchX = event.getRawX()
                        initialTouchY = event.getRawY()
                        lastAction = event.getAction()
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        //As we implemented on touch listener with ACTION_MOVE,
                        //we have to check if the previous action was ACTION_DOWN
                        //to identify if the user clicked the view or not.
                        if (lastAction == MotionEvent.ACTION_DOWN) {
                            //Open the chat conversation click.
                            val intent = Intent(this@DownloadService, MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)

                            //close the service and remove the chat heads
                            stopSelf()
                        }
                        lastAction = event.getAction()
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        //Calculate the X and Y coordinates of the view.
                        params.x = (initialX + (event.getRawX() - initialTouchX)).toInt()
                        params.y = (initialY + (event.getRawY() - initialTouchY)).toInt()

                        //Update the layout with new X & Y coordinate
                        mWindowManager.updateViewLayout(popupView, params)
                        lastAction = event.getAction()
                        return true
                    }

                }
                return false
            }
        })

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")
        startForeground()
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun startForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                "Downloads",
                NotificationManager.IMPORTANCE_LOW
            )
            ContextCompat.getSystemService(this, NotificationManager::class.java)
                ?.createNotificationChannel(notificationChannel)
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Downloads")
                .setContentText("List of all files in the Download Folder")
                .setSmallIcon(R.drawable.ic_download)
                .build()
            startForeground(1, notification)
        }
    }

    private fun getDownloadedFiles(): List<File> {
        /*val mediaStoreUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Downloads.EXTERNAL_CONTENT_URI
        } else {
            TODO("VERSION.SDK_INT < Q")
        }
        val projection = arrayOf(MediaStore.Downloads._ID, MediaStore.Downloads.TITLE)
        val cursor = contentResolver.query(mediaStoreUri, projection, null, null, null)
        val files = mutableListOf<File>()
        if (cursor != null) {
            while (cursor.moveToNext()) {
                val id = cursor.getLong(0)
                val title = cursor.getString(1)
                val file = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    title
                )
                files.add(file)
            }
            cursor.close()
        }
        return files*/
        val downloadFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val files = downloadFolder.listFiles()
        return files.toList()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        mWindowManager.removeView(popupView);
    }

    companion object {
        const val CHANNEL_ID = "downloads"
    }
}
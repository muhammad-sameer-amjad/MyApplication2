package com.downloader.common

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.downloader.R
import com.downloader.interfaces.DialogInterface
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DialogUtil(ctx: Context, val title:String, val message:String,val positiveBtnMsg:String, val negativeBtnMsg:String, val dialogInterface: DialogInterface) {
    private val dialogBuilder = AlertDialog.Builder(ctx)
    fun init(){
        dialogBuilder.setTitle(title)
        dialogBuilder.setMessage(message)
        dialogBuilder.setPositiveButton(positiveBtnMsg) { dialog, which ->
            dialogInterface.positiveDialogButtonPressed()
        }
        dialogBuilder.setNegativeButton(negativeBtnMsg) { dialog, which ->
            dialogInterface.negativeDialogButtonPressed()
        }
    }
    fun showDialog(){
        dialogBuilder.show()
    }


}
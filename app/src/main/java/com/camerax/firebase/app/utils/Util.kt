package com.camerax.firebase.app.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object Util {

     const val DATE_FORMAT = "dd-MM-yyyy HH:mm:ss"

     private const val REQUEST_CODE_PERMISSION = 101

     private val REQUIRED_PERMISSION = arrayOf(
          "android.permission.CAMERA",
          "android.permission.WRITE_EXTERNAL_STORAGE")


     fun permissionGranted(context:Context): Boolean {
          for (permission in REQUIRED_PERMISSION) {
               return ContextCompat.checkSelfPermission(
                    context,
                    permission
               ) == PackageManager.PERMISSION_GRANTED
          }
          return false
     }

     fun requestPermission(context: Context){
          ActivityCompat.requestPermissions(context as Activity, REQUIRED_PERMISSION, REQUEST_CODE_PERMISSION)
     }


     fun getDateAndTime():String{
          val formatter = SimpleDateFormat(DATE_FORMAT)
          val date = Date()
          return formatter.format(date)
     }

     fun getFile():File{
          val path = Environment.getExternalStoragePublicDirectory(
               Environment.DIRECTORY_PICTURES
          )
          val timestamp = System.currentTimeMillis().toString()

          val file = File("$path/$timestamp.jpg")
          return file
     }


}



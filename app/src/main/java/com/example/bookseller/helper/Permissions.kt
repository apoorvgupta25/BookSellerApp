package com.example.bookseller.helper

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class Permissions {

    companion object{
        fun validatePermission(permissions: Array<String>, activity: Activity, requestCode: Int ): Boolean {

            if(Build.VERSION.SDK_INT >= 26){
                val permissionList: MutableList<String> = ArrayList()

                //if permission is not granted, add to List
                for(permission in permissions){
                    if(ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED)
                        permissionList.add(permission)
                }

                if (permissionList.isNullOrEmpty())
                    return true

                val newPermissions: Array<String> = permissionList.toTypedArray()

                ActivityCompat.requestPermissions(activity, newPermissions, requestCode)
            }
            return true
        }
    }
}
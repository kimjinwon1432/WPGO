package com.multi.wpgo

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showPermissionDialog()
    }

    fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    fun exitApp() = finishAffinity()

    private fun showPermissionDialog() {
        val permissionListener: PermissionListener = object : PermissionListener {
            override fun onPermissionGranted() {
                startMainActivity()
            }

            override fun onPermissionDenied(deniedPermissions: List<String>) {
                exitApp()
            }
        }
        TedPermission.with(this)
            .setPermissionListener(permissionListener)
            .setRationaleMessage(resources.getString(R.string.permission_location))
            .setDeniedMessage(resources.getString(R.string.permission_explain))
            .setPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
            .check()
    }
}

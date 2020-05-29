package com.multi.wpgo

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.kotlindemos.OnMapAndViewReadyListener
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.multi.wpgo.data.Pharmacy
import com.multi.wpgo.data.PharmacyRepository
import kotlinx.coroutines.*
import java.io.Serializable
import java.lang.Exception

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showPermissionDialog()
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
//        intent.putExtra("mapData", pharmacies as Serializable)
        startActivity(intent)
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

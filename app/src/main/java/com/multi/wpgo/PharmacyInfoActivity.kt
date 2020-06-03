package com.multi.wpgo

import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.multi.wpgo.data.Pharmacy
import kotlinx.android.synthetic.main.activity_pharmacy_info.*
import kotlinx.android.synthetic.main.bottom_sheet_pharmacy_info.info_address
import kotlinx.android.synthetic.main.bottom_sheet_pharmacy_info.info_name
import kotlinx.android.synthetic.main.bottom_sheet_pharmacy_info.info_tel
import kotlinx.android.synthetic.main.content_pharmacy_info.*

class PharmacyInfoActivity : AppCompatActivity(),
    OnMapReadyCallback{

    private lateinit var pharmacy: Pharmacy

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pharmacy_info)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        if (intent.hasExtra("pharmacy")) {
            pharmacy = intent.getSerializableExtra("pharmacy") as Pharmacy

            Log.e("info", pharmacy.name)
            setData(pharmacy)
        }

        val map = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        map.getMapAsync(this)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
    }

    private fun setData(data: Pharmacy) {
        info_name.text = data.name
        info_address.text = data.addr
        info_tel.text = data.tel
        info_tel.underline()
        info_tel.setOnClickListener{
            val tt = Intent(
                Intent.ACTION_DIAL,
                Uri.parse("tel:" + data.tel)
            )
            startActivity(tt)
        }

        info_weekday.text = data.opWeekday
        info_sat.text = data.opSat
        info_sun.text = data.opSun
    }

    // TextView Underline
    private fun TextView.underline() {
        paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val location = LatLng(pharmacy.lat, pharmacy.lng)

        val iconOpen = BitmapDescriptorFactory.fromResource(R.drawable.ic_pharmacy_open)
        val iconClose = BitmapDescriptorFactory.fromResource(R.drawable.ic_pharmacy_close)

        val marker = MarkerOptions()
            .position(location)
            .icon(if (pharmacy.isOpNow) iconOpen else iconClose)

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 17F))

        googleMap.addMarker(marker)
    }
}

package com.multi.wpgo.ui.home.map

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

import com.multi.wpgo.R
import com.multi.wpgo.ui.home.HomeViewModel

class MapFragment : Fragment(), OnMapReadyCallback {
    private lateinit var map: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_map, container, false)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        mapFragment.getMapAsync(this)
        return root
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.isMyLocationEnabled = true
        val marker = LatLng(37.56, 126.97)
        map.addMarker(MarkerOptions().position(marker).title("서울"))
        map.moveCamera(CameraUpdateFactory.newLatLng(marker))
        map.animateCamera(CameraUpdateFactory.zoomTo(15f))
    }
}

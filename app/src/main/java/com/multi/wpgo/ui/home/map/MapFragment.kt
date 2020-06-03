package com.multi.wpgo.ui.home.map

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.kotlindemos.OnMapAndViewReadyListener
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.*
import com.multi.wpgo.PharmacyInfoActivity
import com.multi.wpgo.R
import com.multi.wpgo.RxBaseFragment
import com.multi.wpgo.data.Pharmacy
import com.multi.wpgo.data.PharmacyRepository
import kotlinx.android.synthetic.main.bottom_sheet_pharmacy_info.*
import kotlinx.coroutines.*
import java.io.Serializable
import java.net.SocketException


class MapFragment : RxBaseFragment(),
    GoogleMap.OnMarkerClickListener,
    GoogleMap.OnInfoWindowClickListener,
    GoogleMap.OnInfoWindowLongClickListener,
    GoogleMap.OnInfoWindowCloseListener,
    OnMapAndViewReadyListener.OnGlobalLayoutAndMapReadyListener {

    private val TAG = MapFragment::class.java.name

    /** This is ok to be lateinit as it is initialised in onMapReady */
    private lateinit var map: GoogleMap

    /**
     * Keeps track of the last selected marker (though it may no longer be selected).  This is
     * useful for refreshing the info window.
     *
     * Must be nullable as it is null when no marker has been selected
     */
    private var lastSelectedMarker: Marker? = null

    private lateinit var pharmacies: List<Pharmacy>

    private lateinit var places: MutableMap<String, LatLng>

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    /** Demonstrates customizing the info window and/or its contents.  */
    internal inner class CustomInfoWindowAdapter : GoogleMap.InfoWindowAdapter {

        // These are both view groups containing an ImageView with id "badge" and two
        // TextViews with id "title" and "snippet".
//        private val window: View = layoutInflater.inflate(R.layout.custom_info_window, null)
        @SuppressLint("InflateParams")
        private val contents: View = layoutInflater.inflate(R.layout.custom_info_contents, null)

        override fun getInfoWindow(marker: Marker): View? {
            return null
        }

        override fun getInfoContents(marker: Marker): View? {
            render(marker, contents)
            return contents
        }

        private fun render(marker: Marker, view: View) {

            // Set the title and snippet for the custom info window
            val title: String? = marker.title
            val titleUi = view.findViewById<TextView>(R.id.title)

            if (title != null) {
                // Spannable string allows us to edit the formatting of the text.
                titleUi.text = SpannableString(title)
            } else {
                titleUi.text = ""
            }

            /*val snippet: String? = marker.snippet
            val snippetUi = view.findViewById<TextView>(R.id.snippet)
            if (snippet != null && snippet.length > 12) {
                snippetUi.text = SpannableString(snippet).apply {
//                    setSpan(ForegroundColorSpan(Color.MAGENTA), 0, 10, 0)
//                    setSpan(ForegroundColorSpan(Color.BLUE), 12, snippet.length, 0)
                }
            } else {
                snippetUi.text = ""
            }*/
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_map, container, false)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        OnMapAndViewReadyListener(mapFragment, this@MapFragment)

        val bottomSheet = root.findViewById<LinearLayout>(R.id.bottom_sheet)
        bottomSheetBehavior = from(bottomSheet)
        bottomSheetBehavior.state = STATE_HIDDEN

        bottomSheet.setOnClickListener {
            val intent = Intent(context, PharmacyInfoActivity::class.java)
            intent.putExtra("pharmacy", pharmacies[Integer.valueOf(lastSelectedMarker?.snippet.toString())] as Serializable)
            startActivity(intent)
        }


//        binding = DataBindingUtil.inflate(inflater, R.layout.bottom_sheet_pharmacy_info, container, false);

        return root
    }

    private fun requestMapData() {
        job = GlobalScope.launch {
            Log.d(TAG, "scope.launch")

            try {
                val repository = PharmacyRepository()
                Log.d(TAG, "서버 데이터 접속 완료")

                val mapData = repository.getPharmacies()
                pharmacies = mapData.pharmacies

                places = mutableMapOf((pharmacies[0].name) to LatLng(pharmacies[0].lat, pharmacies[0].lng))

                if (pharmacies.size > 1) {
                    for (i in 1 until pharmacies.size) {
                        places[pharmacies[i].name] = LatLng(pharmacies[i].lat, pharmacies[i].lng)
                    }
                }

            } catch (se: SocketException) {
                Log.e(TAG, "SocketException: ${se.message}")
            } catch (ex: Throwable) {
                Log.e(TAG, "Error: ${ex.message}")
            }
        }



    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        requestMapData()
    }

    /**
     * This is the callback that is triggered when the GoogleMap has loaded and is ready for use
     */
    override fun onMapReady(googleMap: GoogleMap?) {

        // return early if the map was not initialised properly
        map = googleMap ?: return

        // create bounds that encompass every location we reference
        val boundsBuilder = LatLngBounds.Builder()

        if (::places.isInitialized) {
            // include all places we have markers for on the map
            places.keys.map { place -> boundsBuilder.include(places.getValue(place)) }
            val bounds = boundsBuilder.build()

            with(map) {
                // Hide the zoom controls as the button panel will cover it.
                uiSettings.isZoomControlsEnabled = false

                // Setting an info window adapter allows us to change the both the contents and
                // look of the info window.
                setInfoWindowAdapter(CustomInfoWindowAdapter())

                // Set listeners for marker events.  See the bottom of this class for their behavior.
                setOnMarkerClickListener(this@MapFragment)
                setOnInfoWindowClickListener(this@MapFragment)
                setOnInfoWindowCloseListener(this@MapFragment)
                setOnInfoWindowLongClickListener(this@MapFragment)

                // Override the default content description on the view, for accessibility mode.
                // Ideally this string would be localised.
                setContentDescription("Map with lots of markers.")

                moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150))
            }

            // Add lots of markers to the googleMap.
            addMarkersToMap()
        }
    }

    /**
     * Show all the specified markers on the map
     */
    private fun addMarkersToMap() {
        val iconOpen = BitmapDescriptorFactory.fromResource(R.drawable.ic_pharmacy_open)
        val iconClose = BitmapDescriptorFactory.fromResource(R.drawable.ic_pharmacy_close)

        val placeDetailsMap = mutableMapOf(
            pharmacies[0].name to PlaceDetails(
                position = places.getValue(pharmacies[0].name),
                title = pharmacies[0].name,
                snippet = "${pharmacies[0].id - 1}",
                icon = if (pharmacies[0].isOpNow) iconOpen else iconClose
            )
        )

        if (pharmacies.size > 1) {
            for (i in 1 until pharmacies.size) {
                placeDetailsMap[pharmacies[i].name] = PlaceDetails(
                    position = places.getValue(pharmacies[i].name),
                    title = pharmacies[i].name,
                    snippet = "${pharmacies[i].id - 1}"/*pharmacies[i].addr*/,
                    icon = if (pharmacies[i].isOpNow) iconOpen else iconClose
                )
            }
        }

        // place markers for each of the defined locations
        placeDetailsMap.keys.map {
            with(placeDetailsMap.getValue(it)) {
                map.addMarker(
                    MarkerOptions()
                    .position(position)
                    .title(title)
                    .snippet(snippet)
                    .icon(icon)
                    .infoWindowAnchor(infoWindowAnchorX, infoWindowAnchorY)
                    .draggable(draggable)
                    .zIndex(zIndex))

            }
        }

    }

    private fun setBottomSheetData(data: Pharmacy) {
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

        bottomSheetBehavior.state = STATE_COLLAPSED
    }

    // TextView Underline
    private fun TextView.underline() {
        paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
    }
    //
    // Marker related listeners.
    //
    override fun onMarkerClick(marker : Marker): Boolean {
        // Markers have a z-index that is settable and gettable.
        marker.zIndex += 1.0f

//        Toast.makeText(context, "${marker.title} z-index set to ${marker.zIndex}", Toast.LENGTH_SHORT).show()
        val pharmacy = pharmacies[Integer.valueOf(marker.snippet)]
        setBottomSheetData(pharmacy)

        lastSelectedMarker = marker

        // We return false to indicate that we have not consumed the event and that we wish
        // for the default behavior to occur (which is for the camera to move such that the
        // marker is centered and for the marker's info window to open, if it has one).

        return false
    }

    override fun onInfoWindowClick(marker: Marker) {
        val intent = Intent(context, PharmacyInfoActivity::class.java)
        intent.putExtra("pharmacy", pharmacies[Integer.valueOf(marker.snippet)] as Serializable)
        startActivity(intent)
    }

    override fun onInfoWindowClose(marker : Marker) {
//        Toast.makeText(context, "Close ${marker.title} Info Window", Toast.LENGTH_SHORT).show()
//        val pharmacy = pharmacies[Integer.valueOf(marker.snippet)]
//        info_title.text = ""
        bottomSheetBehavior.state = STATE_HIDDEN
    }

    override fun onInfoWindowLongClick(marker : Marker) {
        Toast.makeText(context, "Info Window long click", Toast.LENGTH_SHORT).show()
    }

    /**
     * Checks if the map is ready, the executes the provided lambda function
     *
     * @param stuffToDo the code to be executed if the map is ready
     */
    private fun checkReadyThen(stuffToDo : () -> Unit) {
        if (!::map.isInitialized) {
            Toast.makeText(context, R.string.map_not_ready, Toast.LENGTH_SHORT).show()
        } else {
            stuffToDo()
        }
    }
}

/**
 * This stores the details of a place that used to draw a marker
 */
class PlaceDetails(
    val position: LatLng,
    val title: String = "Marker",
    val snippet: String? = null,
    val icon: BitmapDescriptor = BitmapDescriptorFactory.defaultMarker(),
    val infoWindowAnchorX: Float = 0.5F,
    val infoWindowAnchorY: Float = 0F,
    val draggable: Boolean = false,
    val zIndex: Float = 0F
)
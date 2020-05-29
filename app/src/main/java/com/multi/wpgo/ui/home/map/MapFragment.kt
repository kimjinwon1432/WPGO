package com.multi.wpgo.ui.home.map

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.kotlindemos.OnMapAndViewReadyListener
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.multi.wpgo.PharmacyInfoActivity

import com.multi.wpgo.R
import com.multi.wpgo.data.Pharmacy
import com.multi.wpgo.data.PharmacyRepository
import kotlinx.coroutines.*
import java.io.Serializable
import java.lang.Exception

class MapFragment : Fragment(),
    GoogleMap.OnMarkerClickListener,
    GoogleMap.OnInfoWindowClickListener,
    GoogleMap.OnInfoWindowLongClickListener,
    GoogleMap.OnInfoWindowCloseListener,
    OnMapAndViewReadyListener.OnGlobalLayoutAndMapReadyListener {

    private val TAG = MapFragment::class.java.name

    /** This is ok to be lateinit as it is initialised in onMapReady */
    private lateinit var map: GoogleMap

    // Coroutine
    private val mainJob = Job()
    private val scope = CoroutineScope(Dispatchers.IO + mainJob)

    /**
     * Keeps track of the last selected marker (though it may no longer be selected).  This is
     * useful for refreshing the info window.
     *
     * Must be nullable as it is null when no marker has been selected
     */
    private var lastSelectedMarker: Marker? = null

    private lateinit var pharmacies: List<Pharmacy>

    private lateinit var places: MutableMap<String, LatLng>

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
                titleUi.text = SpannableString(title).apply {
                    setSpan(ForegroundColorSpan(Color.RED), 0, length, 0)
                }
            } else {
                titleUi.text = ""
            }

            val snippet: String? = marker.snippet
            val snippetUi = view.findViewById<TextView>(R.id.snippet)
            if (snippet != null && snippet.length > 12) {
                snippetUi.text = SpannableString(snippet).apply {
                    setSpan(ForegroundColorSpan(Color.MAGENTA), 0, 10, 0)
                    setSpan(ForegroundColorSpan(Color.BLUE), 12, snippet.length, 0)
                }
            } else {
                snippetUi.text = ""
            }
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

        return root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onStop() {
        super.onStop()
        mainJob.cancel()
    }

    private fun requestMapData() =  scope.launch {

        Log.d("Response", "호출")

        try {
            val repository = PharmacyRepository()
            val work = async {
                val mapData = repository.getPharmacies()
                pharmacies = mapData.pharmacies

                Log.d("Response Success", mapData.toString())

                places = mutableMapOf((pharmacies[0].name) to LatLng(pharmacies[0].lat, pharmacies[0].lng))

                if (pharmacies.size > 1) {
                    for (i in 1 until pharmacies.size) {
                        places[pharmacies[i].name] = LatLng(pharmacies[i].lat, pharmacies[i].lng)
                    }
                }
            }

            val result = work.await()
            Log.d("response work result", result.toString())

        } catch (exception: Exception) {
            exception.printStackTrace()
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

    /**
     * Show all the specified markers on the map
     */
    private fun addMarkersToMap() {
        val placeDetailsMap = mutableMapOf(
            pharmacies[0].name to PlaceDetails(
                position = places.getValue(pharmacies[0].name),
                title = pharmacies[0].name,
                snippet = pharmacies[0].addr
            )
        )

        if (pharmacies.size > 1) {
            for (i in 1 until pharmacies.size) {
                placeDetailsMap[pharmacies[i].name] = PlaceDetails(
                    position = places.getValue(pharmacies[i].name),
                    title = pharmacies[i].name,
                    snippet = pharmacies[i].addr
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

    //
    // Marker related listeners.
    //
    override fun onMarkerClick(marker : Marker): Boolean {

        // Markers have a z-index that is settable and gettable.
        marker.zIndex += 1.0f

        lastSelectedMarker = marker

        // We return false to indicate that we have not consumed the event and that we wish
        // for the default behavior to occur (which is for the camera to move such that the
        // marker is centered and for the marker's info window to open, if it has one).
        return false
    }

    override fun onInfoWindowClick(marker: Marker) {
        startActivity(Intent(activity, PharmacyInfoActivity::class.java))
    }

    override fun onInfoWindowClose(marker : Marker) {
        Toast.makeText(context, "Close Info Window", Toast.LENGTH_SHORT).show()
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
    val zIndex: Float = 0F)
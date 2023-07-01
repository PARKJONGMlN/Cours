package com.pjm.cours.ui.location

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.pjm.cours.databinding.ActivityLocationBinding
import com.pjm.cours.util.Constants
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapReverseGeoCoder
import net.daum.mf.map.api.MapView


class LocationActivity : AppCompatActivity(), MapView.MapViewEventListener,
    MapView.POIItemEventListener, MapReverseGeoCoder.ReverseGeoCodingResultListener {

    private lateinit var binding: ActivityLocationBinding
    private lateinit var mapView: MapView
    private lateinit var selectedPoint: MapPoint
    private lateinit var selectedLocation: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mapView = MapView(this)
        setLayout()
    }

    private fun setLayout() {
        binding.topAppBarLocation.setNavigationOnClickListener {
            finish()
        }

        binding.btnLocationComplete.setOnClickListener {
            val intent = Intent(this, LocationActivity::class.java).apply {
                putExtra(Constants.SELECTED_LOCATION, selectedLocation)
            }
            setResult(RESULT_OK, intent)
            finish()
        }

        mapView.setMapViewEventListener(this)
        mapView.setPOIItemEventListener(this)
        binding.mapView.addView(mapView)
    }

    override fun onMapViewInitialized(p0: MapView?) {

    }

    override fun onMapViewCenterPointMoved(p0: MapView?, p1: MapPoint?) {

    }

    override fun onMapViewZoomLevelChanged(p0: MapView?, p1: Int) {

    }

    override fun onMapViewSingleTapped(mapView: MapView, mapPoint: MapPoint) {
        selectedPoint = mapPoint
        val reverseGeoCoder =
            MapReverseGeoCoder("a54f2e7713031c8b4a83ad68f005f42c", selectedPoint, this, this)
        reverseGeoCoder.startFindingAddress()
    }

    override fun onMapViewDoubleTapped(p0: MapView?, p1: MapPoint?) {

    }

    override fun onMapViewLongPressed(p0: MapView?, p1: MapPoint?) {

    }

    override fun onMapViewDragStarted(p0: MapView?, p1: MapPoint?) {

    }

    override fun onMapViewDragEnded(p0: MapView?, p1: MapPoint?) {

    }

    override fun onMapViewMoveFinished(p0: MapView?, p1: MapPoint?) {

    }

    companion object {
        const val TAG = "LocationActivity"
    }
    // 마커 관련 리스너

    override fun onPOIItemSelected(p0: MapView?, p1: MapPOIItem?) {
        Log.d(TAG, "onPOIItemSelected: ")
    }

    override fun onCalloutBalloonOfPOIItemTouched(p0: MapView?, p1: MapPOIItem?) {

    }

    override fun onCalloutBalloonOfPOIItemTouched(
        p0: MapView?,
        p1: MapPOIItem?,
        p2: MapPOIItem.CalloutBalloonButtonType?
    ) {

    }

    override fun onDraggablePOIItemMoved(p0: MapView?, p1: MapPOIItem?, p2: MapPoint?) {

    }

    override fun onReverseGeoCoderFoundAddress(p0: MapReverseGeoCoder?, result: String) {
        mapView.removeAllPOIItems()
        selectedLocation = result
        val marker = MapPOIItem()
        marker.itemName = result;
        marker.tag = 0;
        marker.mapPoint = selectedPoint
        marker.markerType = MapPOIItem.MarkerType.RedPin
        mapView.addPOIItem(marker)
        binding.btnLocationComplete.isEnabled = true
    }

    override fun onReverseGeoCoderFailedToFindAddress(p0: MapReverseGeoCoder?) {

    }
}
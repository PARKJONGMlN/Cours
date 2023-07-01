package com.pjm.cours.ui.location

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.pjm.cours.BuildConfig
import com.pjm.cours.R
import com.pjm.cours.databinding.ActivityLocationBinding
import com.pjm.cours.util.Constants
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapReverseGeoCoder
import net.daum.mf.map.api.MapView


class LocationActivity : AppCompatActivity(), MapView.MapViewEventListener,
    MapView.POIItemEventListener, MapReverseGeoCoder.ReverseGeoCodingResultListener,
    MapView.CurrentLocationEventListener {

    private lateinit var binding: ActivityLocationBinding
    private lateinit var mapView: MapView
    private lateinit var selectedPoint: MapPoint
    private lateinit var selectedLocation: String
    private var wasSettingsOpened = false
    private var isTrackingMode = false
    private var currentMapPoint: MapPoint? = null

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.entries.forEach { permission ->
            when {
                permission.value -> {
                    setCurrentLocation()
                }
                else -> {
                    Snackbar.make(
                        binding.root,
                        getString(R.string.location_permission_message),
                        Snackbar.LENGTH_SHORT
                    ).apply {
                        setAction(getString(R.string.location_permission_button)) {
                            openSettings()
                        }
                        show()
                    }
                    showMap()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        launchPermission()

        setLayout()
    }

    override fun onResume() {
        super.onResume()
        if (wasSettingsOpened) {
            if (checkLocationPermission()) {
                setCurrentLocation()
            }
        }
    }

    private fun checkLocationPermission() = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

    private fun setCurrentLocation() {
        isTrackingMode = true
        mapView.currentLocationTrackingMode =
            MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading
    }

    private fun openSettings() {
        wasSettingsOpened = true
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }.run(::startActivity)
    }

    private fun setLayout() {
        mapView = MapView(this)
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
        mapView.setCurrentLocationEventListener(this)
        binding.mapView.addView(mapView)

        binding.ivMyLocation.setOnClickListener {
            launchPermission()
            currentMapPoint?.let {
                setCurrentLocation()
                mapView.setMapCenterPoint(it, true)
            }
        }
    }

    private fun launchPermission() {
        locationPermissionRequest.launch(REQUIRED_PERMISSIONS)
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
            MapReverseGeoCoder(BuildConfig.KAKAO_MAP_KEY, selectedPoint, this, this)
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

    override fun onPOIItemSelected(p0: MapView?, p1: MapPOIItem?) {
        mapView.setMapCenterPoint(selectedPoint, true)
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
        mapView.setMapCenterPoint(selectedPoint, true)
        mapView.removeAllPOIItems()
        selectedLocation = result
        val marker = MapPOIItem()
        marker.itemName = result;
        marker.tag = 0;
        marker.mapPoint = selectedPoint
        marker.markerType = MapPOIItem.MarkerType.RedPin
        mapView.addPOIItem(marker)
        mapView.selectPOIItem(marker, true)
        binding.btnLocationComplete.isEnabled = true

    }

    override fun onReverseGeoCoderFailedToFindAddress(p0: MapReverseGeoCoder?) {

    }

    override fun onCurrentLocationUpdate(p0: MapView, mapPoint: MapPoint, p2: Float) {
        val mapPointGeo = mapPoint.mapPointGeoCoord
        currentMapPoint = MapPoint.mapPointWithGeoCoord(mapPointGeo.latitude, mapPointGeo.longitude)
        mapView.setMapCenterPoint(currentMapPoint, true)
        if (isTrackingMode) {
            isTrackingMode = false
            mapView.currentLocationTrackingMode =
                MapView.CurrentLocationTrackingMode.TrackingModeOff
        }
        showMap()
    }

    private fun showMap() {
        lifecycleScope.launch {
            delay(300)
            binding.mapView.visibility = View.VISIBLE
            binding.progressBarMap.visibility = View.GONE
        }
    }

    override fun onCurrentLocationDeviceHeadingUpdate(p0: MapView?, p1: Float) {

    }

    override fun onCurrentLocationUpdateFailed(p0: MapView?) {

    }

    override fun onCurrentLocationUpdateCancelled(p0: MapView?) {

    }

    override fun onStop() {
        super.onStop()
        mapView.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOff
    }

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }
}
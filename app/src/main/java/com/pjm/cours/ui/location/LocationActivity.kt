package com.pjm.cours.ui.location

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
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
    private val viewModel: LocationViewModel by viewModels { LocationViewModel.provideFactory() }
    private lateinit var mapView: MapView

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.entries.forEach { permission ->
            when {
                permission.value -> {
                    if (viewModel.isGrantedPermission.value?.peekContent() != true) {
                        viewModel.setPermission(true)
                    }
                }
                else -> {
                    viewModel.setPermission(false)
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
        setObserver()
    }

    private fun setLayout() {
        mapView = MapView(this)
        mapView.setMapViewEventListener(this)
        mapView.setPOIItemEventListener(this)
        mapView.setCurrentLocationEventListener(this)
        binding.mapView.addView(mapView)

        binding.topAppBarLocation.setNavigationOnClickListener {
            finish()
        }
        binding.btnLocationComplete.setOnClickListener {
            val intent = Intent(this, LocationActivity::class.java).apply {
                putExtra(
                    Constants.SELECTED_LOCATION,
                    viewModel.selectedLocation.value?.peekContent()
                )
                putExtra(
                    Constants.SELECTED_LOCATION_LATITUDE,
                    viewModel.selectedPoint.value?.peekContent()?.mapPointGeoCoord?.latitude.toString()
                )
                putExtra(
                    Constants.SELECTED_LOCATION_LONGITUDE,
                    viewModel.selectedPoint.value?.peekContent()?.mapPointGeoCoord?.longitude.toString()
                )
            }
            setResult(RESULT_OK, intent)
            finish()
        }
        binding.ivMyLocation.setOnClickListener {
            launchPermission()
            viewModel.currentMapPoint.value?.peekContent()?.let {
                viewModel.startTracking()
                mapView.setMapCenterPoint(it, true)
            }
        }
    }

    private fun setObserver() {
        viewModel.isLoading.observe(this) {
            if (!it.peekContent()) {
                showMap()
            }
        }
        viewModel.isGrantedPermission.observe(this) {
            if (it.peekContent()) {
                viewModel.startTracking()
            } else {
                showMap()
                Snackbar.make(
                    binding.root,
                    getString(R.string.location_permission_message),
                    Snackbar.LENGTH_SHORT
                ).apply {
                    setAction(getString(R.string.location_permission_button)) {
                        viewModel.openSetting(true)
                    }
                    show()
                }
            }
        }
        viewModel.isSettingOpened.observe(this) {
            if (it.peekContent()) {
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                }.run(::startActivity)
            }
        }
        viewModel.isTrackingMode.observe(this) {
            if (it.peekContent()) {
                mapView.currentLocationTrackingMode =
                    MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading
            } else {
                mapView.currentLocationTrackingMode =
                    MapView.CurrentLocationTrackingMode.TrackingModeOff
            }
        }
        viewModel.selectedPoint.observe(this) {
            val reverseGeoCoder =
                MapReverseGeoCoder(BuildConfig.KAKAO_MAP_KEY, it.peekContent(), this, this)
            reverseGeoCoder.startFindingAddress()
        }
        viewModel.selectedLocation.observe(this) {
            mapView.setMapCenterPoint(viewModel.selectedPoint.value?.peekContent(), true)
            mapView.removeAllPOIItems()
            val marker = createMarker(it.peekContent())
            mapView.addPOIItem(marker)
            mapView.selectPOIItem(marker, true)
            binding.btnLocationComplete.isEnabled = true
        }
        viewModel.currentMapPoint.observe(this) {
            mapView.setMapCenterPoint(it.peekContent(), true)
            viewModel.endTracking()
            showMap()
        }
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.isSettingOpened.value?.peekContent() == true) {
            viewModel.openSetting(false)
            if (checkLocationPermission()) {
                viewModel.startTracking()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        viewModel.endTracking()
    }

    override fun finish() {
        super.finish()
        binding.mapView.removeView(mapView)
    }

    private fun createMarker(location: String): MapPOIItem {
        val marker = MapPOIItem()
        marker.itemName = location
        marker.tag = 0
        marker.mapPoint = viewModel.selectedPoint.value?.peekContent()
        marker.markerType = MapPOIItem.MarkerType.RedPin
        return marker
    }

    private fun checkLocationPermission() = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

    private fun launchPermission() {
        locationPermissionRequest.launch(REQUIRED_PERMISSIONS)
    }

    private fun showMap() {
        lifecycleScope.launch {
            delay(300)
            binding.mapView.visibility = View.VISIBLE
            binding.progressBarMap.visibility = View.GONE
        }
    }

    override fun onMapViewInitialized(p0: MapView?) {

    }

    override fun onMapViewCenterPointMoved(p0: MapView?, p1: MapPoint?) {

    }

    override fun onMapViewZoomLevelChanged(p0: MapView?, p1: Int) {

    }

    override fun onMapViewSingleTapped(mapView: MapView, mapPoint: MapPoint) {
        viewModel.selectPoint(selectedPoint = mapPoint)
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
        mapView.setMapCenterPoint(viewModel.selectedPoint.value?.peekContent(), true)
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
        viewModel.selectLocation(selectedLocation = result)
    }

    override fun onReverseGeoCoderFailedToFindAddress(p0: MapReverseGeoCoder?) {

    }

    override fun onCurrentLocationUpdate(p0: MapView, mapPoint: MapPoint, p2: Float) {
        val mapPointGeo = mapPoint.mapPointGeoCoord
        val currentMapPoint =
            MapPoint.mapPointWithGeoCoord(mapPointGeo.latitude, mapPointGeo.longitude)
        viewModel.setCurrentMapPoint(currentMapPoint)
    }

    override fun onCurrentLocationDeviceHeadingUpdate(p0: MapView?, p1: Float) {

    }

    override fun onCurrentLocationUpdateFailed(p0: MapView?) {

    }

    override fun onCurrentLocationUpdateCancelled(p0: MapView?) {

    }

    companion object {
        const val TAG = "LocationActivity"
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }
}
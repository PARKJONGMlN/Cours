package com.pjm.cours.ui.map

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.snackbar.Snackbar
import com.pjm.cours.CoursApplication
import com.pjm.cours.R
import com.pjm.cours.data.repository.PostRepository
import com.pjm.cours.data.model.PostPreview
import com.pjm.cours.databinding.FragmentMapBinding
import com.pjm.cours.ui.BaseFragment
import com.pjm.cours.ui.postcomposition.PostCompositionActivity
import com.pjm.cours.ui.postdetail.PostDetailActivity
import com.pjm.cours.util.Constants
import com.pjm.cours.util.EventObserver
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView

class MapFragment : BaseFragment<FragmentMapBinding>(R.layout.fragment_map),
    MapView.MapViewEventListener, MapView.POIItemEventListener,
    MapView.CurrentLocationEventListener {

    private val viewModel: MapViewModel by viewModels {
        MapViewModel.provideFactory(
            PostRepository(
                CoursApplication.apiContainer.provideApiClient(),
                CoursApplication.preferencesManager
            )
        )
    }
    private lateinit var mapView: MapView
    private lateinit var adapter: PostPreviewAdapter

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

    override fun onStart() {
        super.onStart()
        viewModel.getPosts()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        launchPermission()
    }

    private fun initMapView() {
        mapView = MapView(requireContext())
        mapView.setMapViewEventListener(this)
        mapView.setPOIItemEventListener(this)
        mapView.setCurrentLocationEventListener(this)
        binding.mapView.addView(mapView)
    }

    private fun setLayout() {
        binding.appBarMap.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.create_post -> {
                    startActivity(Intent(requireContext(), PostCompositionActivity::class.java))
                    true
                }
                else -> false
            }
        }
        setViewPagerForm()
        binding.ivMyLocationMap.setOnClickListener {
            launchPermission()
            viewModel.currentMapPoint.value?.peekContent()?.let {
                viewModel.startTracking()
                mapView.setMapCenterPoint(it, true)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        initMapView()
        setLayout()
        setObserver()
        if (viewModel.isSettingOpened.value?.peekContent() == true) {
            viewModel.openSetting(false)
            if (checkLocationPermission()) {
                viewModel.startTracking()
            }
        }
    }

    private fun setObserver() {
        viewModel.isLoading.observe(this, EventObserver { isLoading ->
            if (isLoading) {
                showMap()
            }
        })
        viewModel.isCompleted.observe(this, EventObserver { isCompleted ->
            if (isCompleted) {
                viewModel.postPreviewList.value?.let { postPreviewList ->
                    setMarker(postPreviewList)
                    adapter.submitList(postPreviewList)
                }
            }
        })
        viewModel.isGrantedPermission.observe(this, EventObserver { isGrantedPermission ->
            if (isGrantedPermission) {
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
        })
        viewModel.isSettingOpened.observe(this, EventObserver { isSettingOpened ->
            if (isSettingOpened) {
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", requireContext().packageName, null)
                }.run(::startActivity)
            }
        })
        viewModel.isTrackingMode.observe(this, EventObserver { isTrackingMode ->
            if (isTrackingMode) {
                mapView.currentLocationTrackingMode =
                    MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading
            } else {
                mapView.currentLocationTrackingMode =
                    MapView.CurrentLocationTrackingMode.TrackingModeOff
            }
        })
        viewModel.currentMapPoint.observe(this) {
            mapView.setMapCenterPoint(it.peekContent(), true)
            viewModel.endTracking()
            showMap()
        }
    }

    private fun setMarker(postPreviewList: List<PostPreview>) {
        mapView.removeAllPOIItems()
        val markerArr = ArrayList<MapPOIItem>()
        for (data in postPreviewList) {
            val marker = MapPOIItem()
            marker.mapPoint = MapPoint.mapPointWithGeoCoord(
                data.latitude.toDouble(),
                data.longitude.toDouble()
            )
            marker.markerType = MapPOIItem.MarkerType.BluePin
            marker.selectedMarkerType = MapPOIItem.MarkerType.RedPin
            marker.itemName = data.location
            markerArr.add(marker)
            mapView.addPOIItem(marker)
        }
    }

    private fun setViewPagerForm() {
        adapter = PostPreviewAdapter { preview ->
            val intent = Intent(requireContext(), PostDetailActivity::class.java)
            intent.putExtra(Constants.POST_ID, preview.postId)
            intent.putExtra(Constants.POST_DISTANCE, preview.distance)
            startActivity(intent)
        }
        with(binding.viewPagerMap) {
            adapter = this@MapFragment.adapter
            val pageWidth = resources.getDimension(R.dimen.viewpager_item_width)
            val pageMargin = resources.getDimension(R.dimen.viewpager_item_margin)
            val screenWidth = resources.displayMetrics.widthPixels
            val offset = screenWidth - pageWidth - pageMargin
            offscreenPageLimit = 2
            setPageTransformer { page, position ->
                page.translationX = position * -offset
            }
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    mapView.selectPOIItem(mapView.poiItems[position], true)
                    mapView.setMapCenterPoint(mapView.poiItems[position].mapPoint, true)
                }
            })
        }
    }

    private fun checkLocationPermission() = ContextCompat.checkSelfPermission(
        requireContext(),
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                requireContext(),
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

    override fun onMapViewSingleTapped(p0: MapView?, p1: MapPoint?) {
        binding.viewPagerMap.visibility = View.GONE
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
        mapView.setMapCenterPoint(p1?.mapPoint, true)
        binding.viewPagerMap.visibility = View.VISIBLE
        binding.viewPagerMap.currentItem = mapView.poiItems.indexOf(p1)
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

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    override fun onCurrentLocationUpdate(p0: MapView?, mapPoint: MapPoint, p2: Float) {
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
}
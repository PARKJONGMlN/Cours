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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.snackbar.Snackbar
import com.pjm.cours.R
import com.pjm.cours.data.model.PostPreview
import com.pjm.cours.databinding.FragmentMapBinding
import com.pjm.cours.ui.BaseFragment
import com.pjm.cours.ui.main.MainFragment
import com.pjm.cours.ui.postcomposition.PostCompositionActivity
import com.pjm.cours.ui.postdetail.PostDetailActivity
import com.pjm.cours.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView

@AndroidEntryPoint
class MapFragment : BaseFragment<FragmentMapBinding>(R.layout.fragment_map),
    MapView.MapViewEventListener, MapView.POIItemEventListener,
    MapView.CurrentLocationEventListener {

    private val viewModel: MapViewModel by viewModels()
    private lateinit var mapView: MapView
    private lateinit var adapter: PostPreviewAdapter
    private val locationPermissionRequest = getActivityResultLauncher()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        launchPermission()
        setLayout()
    }

    override fun onStart() {
        super.onStart()
        initMapView()
        if (viewModel.isSettingOpened.value) {
            viewModel.openSetting(false)
            if (checkLocationPermission()) {
                viewModel.startTracking()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.removeAllViews()
    }

    private fun setLayout() {
        setAppBar()
        setCurrentLocation()
        setErrorMessage()
        setLoading()
        setPostPreviewList()
        setPermission()
        setRequestPermission()
        setMapTracking()
        setCurrentMapPoint()
    }

    private fun getActivityResultLauncher() = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.entries.forEach { permission ->
            when {
                permission.value -> {
                    viewModel.setPermission(true)
                }
                else -> {
                    viewModel.setPermission(false)
                }
            }
        }
    }

    private fun setRequestPermission() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isSettingOpened.flowWithLifecycle(
                viewLifecycleOwner.lifecycle,
                Lifecycle.State.STARTED
            ).collect { isSettingOpened ->
                if (isSettingOpened) {
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", requireContext().packageName, null)
                    }.run(::startActivity)
                }
            }
        }
    }

    private fun initMapView() {
        setViewPagerForm()
        viewModel.getPosts()
        mapView = MapView(requireActivity())
        mapView.setMapViewEventListener(this)
        mapView.setPOIItemEventListener(this)
        mapView.setCurrentLocationEventListener(this)
        binding.mapView.addView(mapView)
    }

    private fun setAppBar() {
        binding.appBarMap.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.create_post -> {
                    startActivity(Intent(requireContext(), PostCompositionActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun setCurrentLocation() {
        binding.ivMyLocationMap.setOnClickListener {
            launchPermission()
            binding.viewPagerMap.visibility = View.GONE
            viewModel.currentMapPoint.value?.let { currentMapPoint ->
                mapView.setMapCenterPoint(currentMapPoint, true)
                viewModel.startTracking()
                viewModel.getPosts()
            }
        }
    }

    private fun setErrorMessage() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isError.flowWithLifecycle(
                viewLifecycleOwner.lifecycle,
                Lifecycle.State.STARTED
            ).collect { isError ->
                if (isError) {
                    (parentFragment as MainFragment).showSnackBar(getString(R.string.error_message))
                    showMap()
                }
            }
        }
    }

    private fun setPostPreviewList() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.postPreviewList.flowWithLifecycle(
                viewLifecycleOwner.lifecycle,
                Lifecycle.State.STARTED
            ).collect { postPreviewList ->
                if (postPreviewList.isNotEmpty()) {
                    adapter.submitList(postPreviewList)
                    setMarker(postPreviewList)
                }
            }
        }
    }

    private fun setLoading() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.flowWithLifecycle(
                viewLifecycleOwner.lifecycle,
                Lifecycle.State.STARTED
            ).collect { isLoading ->
                if (isLoading) {
                    showMap()
                }
            }
        }
    }

    private fun setCurrentMapPoint() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentMapPoint.flowWithLifecycle(
                viewLifecycleOwner.lifecycle,
                Lifecycle.State.STARTED
            ).collect { mapPoint ->
                if (mapPoint != null) {
                    viewModel.endTracking()
                    showMap()
                }
            }
        }
    }

    private fun setMapTracking() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isTrackingMode.flowWithLifecycle(
                viewLifecycleOwner.lifecycle,
                Lifecycle.State.STARTED
            ).collect { isTrackingMode ->
                if (isTrackingMode) {
                    mapView.currentLocationTrackingMode =
                        MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading
                } else {
                    mapView.currentLocationTrackingMode =
                        MapView.CurrentLocationTrackingMode.TrackingModeOff
                }
            }
        }
    }

    private fun setPermission() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isGrantedPermission.flowWithLifecycle(
                viewLifecycleOwner.lifecycle,
                Lifecycle.State.STARTED
            ).collect { isGrantedPermission ->
                if (isGrantedPermission) {
                    viewModel.startTracking()
                } else {
                    showMap()
                    showSnackBar()
                }
            }
        }
    }

    private fun showSnackBar() {
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
                    if (position < mapView.poiItems.size) {
                        mapView.selectPOIItem(mapView.poiItems[position], true)
                        mapView.setMapCenterPoint(mapView.poiItems[position].mapPoint, true)
                    }
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
            _binding?.let {
                binding.mapView.visibility = View.VISIBLE
                binding.progressBarMap.visibility = View.GONE
            }
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
        binding.viewPagerMap.currentItem = mapView.poiItems.indexOf(p1)
        binding.viewPagerMap.visibility = View.VISIBLE
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
        mapView.setMapCenterPoint(mapPoint, true)
        viewModel.setCurrentMapPoint(currentMapPoint)
    }

    override fun onCurrentLocationDeviceHeadingUpdate(p0: MapView?, p1: Float) {

    }

    override fun onCurrentLocationUpdateFailed(p0: MapView?) {

    }

    override fun onCurrentLocationUpdateCancelled(p0: MapView?) {

    }
}
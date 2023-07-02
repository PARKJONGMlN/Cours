package com.pjm.cours.ui.map

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.pjm.cours.CoursApplication
import com.pjm.cours.R
import com.pjm.cours.data.PostRepository
import com.pjm.cours.data.model.Post
import com.pjm.cours.databinding.ActivityMapBinding
import com.pjm.cours.ui.postcomposition.PostCompositionActivity
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import net.daum.mf.map.api.MapView.MapViewEventListener
import net.daum.mf.map.api.MapView.POIItemEventListener


class MapActivity : AppCompatActivity(), MapViewEventListener, POIItemEventListener {

    private lateinit var binding: ActivityMapBinding
    private val viewModel: MapViewModel by viewModels {
        MapViewModel.provideFactory(PostRepository(CoursApplication.apiContainer.provideApiClient()))
    }
    private lateinit var mapView: MapView
    private var makerList: List<Post>? = listOf()
    private var stringList: List<String> = listOf()
    private val adapter = PreviewAdapter()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initMapView()
        setLayout()
        setObserver()
    }

    private fun initMapView() {
        viewModel.getPosts()
        mapView = MapView(this)
        mapView.setMapViewEventListener(this)
        mapView.setPOIItemEventListener(this)
        binding.mapView.addView(mapView)
    }

    private fun setLayout() {
        binding.appBarMap.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.create_post -> {
                    startActivity(Intent(this, PostCompositionActivity::class.java))
                    true
                }
                else -> false
            }
        }
        binding.viewPagerMap.adapter = adapter
    }

    override fun onRestart() {
        super.onRestart()
        initMapView()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.removeView(mapView)
    }

    private fun setObserver() {
        viewModel.isCompleted.observe(this) { isCompleted ->
            if (isCompleted) {
                makerList = viewModel.postList
                val markerArr = ArrayList<MapPOIItem>()
                for (data in makerList!!) {
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
                stringList = makerList!!.map { it.location }
                adapter.submitList(stringList)
                with(binding.viewPagerMap) {
                    val pageWidth = resources.getDimension(R.dimen.viewpager_item_width)
                    val pageMargin = resources.getDimension(R.dimen.viewpager_item_margin)
                    val screenWidth = resources.displayMetrics.widthPixels
                    val offset = screenWidth - pageWidth - pageMargin

                    offscreenPageLimit = 2
                    setPageTransformer { page, position ->
                        page.translationX = position * -offset
                    }
                    registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                        override fun onPageScrolled(
                            position: Int,
                            positionOffset: Float,
                            positionOffsetPixels: Int
                        ) {
                            super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                        }

                        override fun onPageSelected(position: Int) {
                            super.onPageSelected(position)
                            mapView.selectPOIItem(mapView.poiItems[position], true)
                            mapView.setMapCenterPoint(mapView.poiItems[position].mapPoint, true)

                        }

                        override fun onPageScrollStateChanged(state: Int) {
                            super.onPageScrollStateChanged(state)
                        }
                    })
                }

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
}
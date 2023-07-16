package com.pjm.cours.ui.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pjm.cours.data.model.PostPreview
import com.pjm.cours.data.repository.PostRepository
import com.pjm.cours.util.DistanceManager
import com.pjm.cours.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import net.daum.mf.map.api.MapPoint
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val repository: PostRepository
) : ViewModel() {

    private val _isLoading = MutableLiveData<Event<Boolean>>()
    val isLoading: LiveData<Event<Boolean>> = _isLoading

    private val _isSettingOpened = MutableLiveData(Event(false))
    val isSettingOpened: LiveData<Event<Boolean>> = _isSettingOpened

    private val _isTrackingMode = MutableLiveData(Event(false))
    val isTrackingMode: LiveData<Event<Boolean>> = _isTrackingMode

    private val _currentMapPoint = MutableLiveData<Event<MapPoint>>()
    val currentMapPoint: LiveData<Event<MapPoint>> = _currentMapPoint

    private val _isGrantedPermission = MutableLiveData<Event<Boolean>>()
    val isGrantedPermission: LiveData<Event<Boolean>> = _isGrantedPermission

    private val _isCompleted = MutableLiveData(Event(false))
    val isCompleted: LiveData<Event<Boolean>> = _isCompleted

    private val _postPreviewList = MutableLiveData<List<PostPreview>>()
    val postPreviewList: LiveData<List<PostPreview>> = _postPreviewList

    fun setUiState(){
        _isCompleted.value = Event(true)
    }

    fun setPermission(boolean: Boolean) {
        _isGrantedPermission.value = Event(boolean)
    }

    fun openSetting(boolean: Boolean) {
        _isSettingOpened.value = Event(boolean)
    }

    fun startTracking() {
        _isTrackingMode.value = Event(true)
    }

    fun endTracking() {
        _isTrackingMode.value = Event(false)
    }

    fun setCurrentMapPoint(currentMapPoint: MapPoint) {
        _currentMapPoint.value = Event(currentMapPoint)
        calculateDistance()
        _isLoading.value = Event(false)
    }

    private fun calculateDistance() {
        val currentUserLatitude = _currentMapPoint.value?.peekContent()?.mapPointGeoCoord?.latitude
        val currentUserLongitude =
            _currentMapPoint.value?.peekContent()?.mapPointGeoCoord?.longitude
        if (currentUserLatitude != null && currentUserLongitude != null) {
            _postPreviewList.value = postPreviewList.value?.map {
                PostPreview(
                    postId = it.postId,
                    title = it.title,
                    currentMemberCount = it.currentMemberCount,
                    location = it.location,
                    latitude = it.latitude,
                    longitude = it.longitude,
                    category = it.category,
                    language = it.language,
                    distance = DistanceManager.getDistance(
                        currentUserLatitude.toDouble(),
                        currentUserLongitude.toDouble(),
                        it.latitude.toDouble(),
                        it.longitude.toDouble()
                    ).toString()
                )
            }?.sortedBy {
                it.distance.toInt()
            }
            _isCompleted.value = Event(true)
        }
    }

    fun getPosts() {
        viewModelScope.launch {
            val result = repository.getPostList()
            val postList = result.body()
            if (result.isSuccessful && postList != null) {
                result.body()
                _postPreviewList.value = postList.map {
                    PostPreview(
                        postId = it.key,
                        title = it.value.title,
                        currentMemberCount = it.value.currentMemberCount,
                        location = it.value.location,
                        latitude = it.value.latitude,
                        longitude = it.value.longitude,
                        category = it.value.category,
                        language = it.value.language
                    )
                }
                _isCompleted.value = Event(true)
            }
        }
    }
}
package com.pjm.cours.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pjm.cours.data.model.PostPreview
import com.pjm.cours.data.repository.PostRepository
import com.pjm.cours.util.DistanceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.daum.mf.map.api.MapPoint
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val repository: PostRepository
) : ViewModel() {

    private val _isGrantedPermission = MutableSharedFlow<Boolean>()
    val isGrantedPermission = _isGrantedPermission.asSharedFlow()

    private val _isError = MutableStateFlow(false)
    val isError = _isError.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _isSettingOpened = MutableStateFlow(false)
    val isSettingOpened = _isSettingOpened.asStateFlow()

    private val _isTrackingMode = MutableStateFlow(false)
    val isTrackingMode = _isTrackingMode.asSharedFlow()

    private val _currentMapPoint = MutableStateFlow<MapPoint?>(null)
    val currentMapPoint = _currentMapPoint.asStateFlow()

    private val _postPreviewList = MutableStateFlow<List<PostPreview>>(emptyList())
    val postPreviewList = _postPreviewList.asStateFlow()


    fun setPermission(boolean: Boolean) {
        viewModelScope.launch {
            _isGrantedPermission.emit(boolean)
        }
    }

    fun openSetting(boolean: Boolean) {
        _isSettingOpened.value = boolean
    }

    fun startTracking() {
        viewModelScope.launch {
            _isTrackingMode.emit(true)
        }
    }

    fun endTracking() {
        viewModelScope.launch {
            _isTrackingMode.emit(false)
        }
    }

    fun setCurrentMapPoint(currentMapPoint: MapPoint) {
        _currentMapPoint.value = currentMapPoint
        repository.setUserCurrentPoint(currentMapPoint)
        getPosts()
    }

    fun getPosts() {
        viewModelScope.launch {
            val currentMapPoint = repository.getUserCurrentPoint()
            if (currentMapPoint == null) {
                setPostPreviewList()
            } else {
                setPostPreviewListHaveCurrentMap(currentMapPoint)
            }
        }
    }

    private suspend fun setPostPreviewListHaveCurrentMap(currentMapPoint: MapPoint) {
        repository.getPostList(
            onSuccess = {
                _isLoading.value = false
                _isError.value = false
            }, onError = {
                _isLoading.value = false
                _isError.value = true
            }).collect { postList ->
            _postPreviewList.value = postList.map { post ->
                PostPreview(
                    postId = post.key,
                    title = post.title,
                    currentMemberCount = post.currentMemberCount,
                    limitMemberCount = post.limitMemberCount,
                    location = post.location,
                    latitude = post.latitude,
                    longitude = post.longitude,
                    category = post.category,
                    language = post.language,
                    distance = DistanceManager.getDistance(
                        currentMapPoint.mapPointGeoCoord.latitude,
                        currentMapPoint.mapPointGeoCoord.longitude,
                        post.latitude.toDouble(),
                        post.longitude.toDouble()
                    ).toString(),
                    hostImageUri = post.hostUser.profileUri
                )
            }.sortedBy {
                it.distance.toInt()
            }
        }
    }

    private suspend fun setPostPreviewList() {
        repository.getPostList(
            onSuccess = {
                _isLoading.value = false
            }, onError = {
                _isLoading.value = false
                _isError.value = true
            }).collect { postList ->
            _postPreviewList.value = postList.map { post ->
                PostPreview(
                    postId = post.key,
                    title = post.title,
                    currentMemberCount = post.currentMemberCount,
                    limitMemberCount = post.limitMemberCount,
                    location = post.location,
                    latitude = post.latitude,
                    longitude = post.longitude,
                    category = post.category,
                    language = post.language,
                    hostImageUri = post.hostUser.profileUri
                )
            }
        }
    }

}
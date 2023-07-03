package com.pjm.cours.ui.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pjm.cours.data.PostRepository
import com.pjm.cours.data.model.Post
import com.pjm.cours.data.model.PostPreview
import com.pjm.cours.util.Event
import kotlinx.coroutines.launch
import net.daum.mf.map.api.MapPoint

class MapViewModel(
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

    private val _isCompleted = MutableLiveData(false)
    val isCompleted: LiveData<Boolean> = _isCompleted

    var postList: List<Post>? = listOf()
    var postPreviewList: List<PostPreview>? = listOf()

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
        _isLoading.value = Event(false)
    }

    fun getPosts() {
        viewModelScope.launch {
            val result = repository.getPostList()
            postPreviewList = result.body()?.map {
                PostPreview(
                    postId = it.key,
                    title = it.value.title,
                    currentMemberCount = it.value.numberOfMember,
                    location = it.value.location,
                    latitude = it.value.latitude,
                    longitude = it.value.longitude,
                    category = it.value.category,
                    language = it.value.language
                )
            }
            postList = result.body()?.values?.toList()
            _isCompleted.value = true
        }
    }

    companion object {

        fun provideFactory(repository: PostRepository) = viewModelFactory {
            initializer {
                MapViewModel(repository)
            }
        }
    }
}
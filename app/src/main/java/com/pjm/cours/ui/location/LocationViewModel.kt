package com.pjm.cours.ui.location

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pjm.cours.util.Event
import net.daum.mf.map.api.MapPoint

class LocationViewModel : ViewModel() {

    private val _isLoading = MutableLiveData<Event<Boolean>>()
    val isLoading: LiveData<Event<Boolean>> = _isLoading

    private val _selectedPoint = MutableLiveData<Event<MapPoint>>()
    val selectedPoint: LiveData<Event<MapPoint>> = _selectedPoint

    private val _selectedLocation = MutableLiveData<Event<String>>()
    val selectedLocation: LiveData<Event<String>> = _selectedLocation

    private val _isSettingOpened = MutableLiveData(Event(false))
    val isSettingOpened: LiveData<Event<Boolean>> = _isSettingOpened

    private val _isTrackingMode = MutableLiveData(Event(false))
    val isTrackingMode: LiveData<Event<Boolean>> = _isTrackingMode

    private val _currentMapPoint = MutableLiveData<Event<MapPoint>>()
    val currentMapPoint: LiveData<Event<MapPoint>> = _currentMapPoint

    private val _isGrantedPermission = MutableLiveData<Event<Boolean>>()
    val isGrantedPermission: LiveData<Event<Boolean>> = _isGrantedPermission

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

    fun selectPoint(selectedPoint: MapPoint) {
        _selectedPoint.value = Event(selectedPoint)
    }

    fun selectLocation(selectedLocation: String) {
        _selectedLocation.value = Event(selectedLocation)
    }

    fun setCurrentMapPoint(currentMapPoint: MapPoint) {
        _currentMapPoint.value = Event(currentMapPoint)
        _isLoading.value = Event(false)
    }

    companion object {

        fun provideFactory() = viewModelFactory {
            initializer {
                LocationViewModel()
            }
        }
    }
}
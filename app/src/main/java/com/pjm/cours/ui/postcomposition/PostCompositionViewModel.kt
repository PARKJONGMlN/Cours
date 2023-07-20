package com.pjm.cours.ui.postcomposition

import android.util.Log
import androidx.lifecycle.*
import com.pjm.cours.data.remote.ApiResultError
import com.pjm.cours.data.remote.ApiResultException
import com.pjm.cours.data.remote.ApiResultSuccess
import com.pjm.cours.data.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostCompositionViewModel @Inject constructor(
    private val repository: PostRepository
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    private val _isCompleted = MutableLiveData(false)
    val isCompleted: LiveData<Boolean> = _isCompleted
    private val _isError = MutableLiveData(false)
    val isError: LiveData<Boolean> = _isError

    val postId = MutableLiveData<String>()
    val title = MutableLiveData<String>()
    val body = MutableLiveData<String>()
    val numberOfMember = MutableLiveData<String>()

    private val _location = MutableLiveData<String>()
    val location: LiveData<String> = _location

    private val _locationLatitude = MutableLiveData<String>()
    private val _locationLongitude = MutableLiveData<String>()

    private val _meetingDate = MutableLiveData<String>()
    val meetingDate: LiveData<String> = _meetingDate

    private val _category = MutableLiveData<String>()
    val category: LiveData<String> = _category

    private val _language = MutableLiveData<String>()
    val language: LiveData<String> = _language

    val isLocationSelected = MutableLiveData(false)
    val isMeetingDateSelected = MutableLiveData(false)
    val isCategorySelected = MutableLiveData(false)
    val isLanguageSelected = MutableLiveData(false)

    val isInputComplete = MediatorLiveData<Boolean>().apply {
        addSource(title) { value = checkInputs() }
        addSource(body) { value = checkInputs() }
        addSource(numberOfMember) { value = checkInputs() }
        addSource(isLocationSelected) { value = checkInputs() }
        addSource(isMeetingDateSelected) { value = checkInputs() }
        addSource(isCategorySelected) { value = checkInputs() }
        addSource(isLanguageSelected) { value = checkInputs() }
    }

    fun setLocation(location: String) {
        _location.value = location
    }

    fun setLocationPoint(latitude: String, longitude: String) {
        _locationLatitude.value = latitude
        _locationLongitude.value = longitude
    }

    fun setMeetingDate(location: String) {
        _meetingDate.value = location
    }

    fun setCategory(location: String) {
        _category.value = location
    }

    fun setLanguage(location: String) {
        _language.value = location
    }

    fun setLocationSelection(boolean: Boolean) {
        isLocationSelected.value = boolean
    }

    fun setMeetingDateSelection(boolean: Boolean) {
        isMeetingDateSelected.value = boolean
    }

    fun setCategorySelection(boolean: Boolean) {
        isCategorySelected.value = boolean
    }

    fun setLanguageSelection(boolean: Boolean) {
        isLanguageSelected.value = boolean
    }

    fun createPost() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.createPost(
                title = title.value ?: "",
                body = body.value ?: "",
                limitMemberCount = numberOfMember.value ?: "",
                location = _location.value ?: "",
                latitude = _locationLatitude.value ?: "",
                longitude = _locationLongitude.value ?: "",
                meetingDate = _meetingDate.value ?: "",
                category = _category.value ?: "",
                language = _language.value ?: ""
            )
            _isLoading.value = false
            when (result) {
                is ApiResultSuccess -> {
                    postId.value = result.data["name"]
                    _isCompleted.value = true
                }
                is ApiResultError -> {
                    Log.d("TAG", "ApiResultError: code ${result.code} message ${result.message}")
                    _isError.value = true
                }
                is ApiResultException -> {
                    Log.d("TAG", "ApiResultException: ${result.throwable.message} ")
                    _isError.value = true
                }
            }
        }
    }

    private fun checkInputs(): Boolean {
        val currentTitle = title.value ?: ""
        val currentBody = body.value ?: ""
        val currentNumberOfMember = numberOfMember.value ?: ""
        return currentTitle.isNotBlank()
                && currentBody.isNotBlank()
                && currentNumberOfMember.isNotBlank()
                && isLocationSelected.value ?: false
                && isMeetingDateSelected.value ?: false
                && isCategorySelected.value ?: false
                && isLanguageSelected.value ?: false
    }
}
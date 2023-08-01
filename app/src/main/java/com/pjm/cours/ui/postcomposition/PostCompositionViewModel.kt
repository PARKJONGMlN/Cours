package com.pjm.cours.ui.postcomposition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pjm.cours.data.remote.ApiResultError
import com.pjm.cours.data.remote.ApiResultException
import com.pjm.cours.data.remote.ApiResultSuccess
import com.pjm.cours.data.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostCompositionViewModel @Inject constructor(
    private val repository: PostRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    private val _isCompleted = MutableStateFlow(false)
    val isCompleted = _isCompleted.asStateFlow()
    private val _isError = MutableStateFlow(false)
    val isError = _isError.asStateFlow()

    val postId = MutableStateFlow("")
    val title = MutableStateFlow("")
    val body = MutableStateFlow("")
    val numberOfMember = MutableStateFlow("")

    private val _location = MutableStateFlow("")
    val location = _location.asStateFlow()

    private val _locationLatitude = MutableStateFlow("")
    private val _locationLongitude = MutableStateFlow("")

    private val _meetingDate = MutableStateFlow("")
    val meetingDate = _meetingDate.asStateFlow()

    private val _category = MutableStateFlow("")
    val category = _category.asStateFlow()

    private val _language = MutableStateFlow("")
    val language = _language.asStateFlow()

    val isLocationSelected = MutableStateFlow(false)
    val isMeetingDateSelected = MutableStateFlow(false)
    val isCategorySelected = MutableStateFlow(false)
    val isLanguageSelected = MutableStateFlow(false)

    val isInputComplete: StateFlow<Boolean> =
        combine(
            title,
            body,
            numberOfMember,
            isLocationSelected,
            isMeetingDateSelected,
            isCategorySelected,
            isLanguageSelected
        ) {
            checkInputs()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

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
                title = title.value,
                body = body.value,
                limitMemberCount = numberOfMember.value,
                location = _location.value,
                latitude = _locationLatitude.value,
                longitude = _locationLongitude.value,
                meetingDate = _meetingDate.value,
                category = _category.value,
                language = _language.value
            )
            _isLoading.value = false
            when (result) {
                is ApiResultSuccess -> {
                    postId.value = result.data["name"] ?: ""
                    repository.setChatPreview(postId.value)
                    _isCompleted.value = true
                }
                is ApiResultError -> {
                    _isError.value = true
                }
                is ApiResultException -> {
                    _isError.value = true
                }
            }
        }
    }

    private fun checkInputs(): Boolean {
        val currentTitle = title.value
        val currentBody = body.value
        val currentNumberOfMember = numberOfMember.value
        return currentTitle.isNotBlank()
                && currentBody.isNotBlank()
                && currentNumberOfMember.isNotBlank()
                && isLocationSelected.value
                && isMeetingDateSelected.value
                && isCategorySelected.value
                && isLanguageSelected.value
    }

}
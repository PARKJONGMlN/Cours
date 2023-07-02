package com.pjm.cours.ui.postcomposition

import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pjm.cours.data.PostCompositionRepository
import com.pjm.cours.util.Event
import kotlinx.coroutines.launch
import retrofit2.HttpException

class PostCompositionViewModel(
    private val repository: PostCompositionRepository
) : ViewModel() {

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading
    private val _isCompleted = MutableLiveData(false)
    val isCompleted: LiveData<Boolean> = _isCompleted
    private val _isError = MutableLiveData(false)
    val isError: LiveData<Boolean> = _isError

    val title = MutableLiveData<String>()
    val body = MutableLiveData<String>()
    val numberOfMember = MutableLiveData<String>()

    private val _location = MutableLiveData<Event<String>>()
    val location: LiveData<Event<String>> = _location

    private val _locationLatitude = MutableLiveData<Event<String>>()
    private val _locationLongitude = MutableLiveData<Event<String>>()

    private val _meetingDate = MutableLiveData<Event<String>>()
    val meetingDate: LiveData<Event<String>> = _meetingDate

    private val _category = MutableLiveData<Event<String>>()
    val category: LiveData<Event<String>> = _category

    private val _language = MutableLiveData<Event<String>>()
    val language: LiveData<Event<String>> = _language

    private val _isLocationSelected = MutableLiveData<Event<Boolean>>()
    private val _isMeetingDateSelected = MutableLiveData<Event<Boolean>>()
    private val _isCategorySelected = MutableLiveData<Event<Boolean>>()
    private val _isLanguageSelected = MutableLiveData<Event<Boolean>>()

    val isInputComplete = MediatorLiveData<Boolean>().apply {
        addSource(title) { value = checkInputs() }
        addSource(body) { value = checkInputs() }
        addSource(numberOfMember) { value = checkInputs() }
        addSource(_isLocationSelected) { value = checkInputs() }
        addSource(_isMeetingDateSelected) { value = checkInputs() }
        addSource(_isCategorySelected) { value = checkInputs() }
        addSource(_isLanguageSelected) { value = checkInputs() }
    }

    fun setLocation(location: String) {
        _location.value = Event(location)
    }

    fun setLocationPoint(latitude: String, longitude: String) {
        _locationLatitude.value = Event(latitude)
        _locationLongitude.value = Event(longitude)
    }

    fun setMeetingDate(location: String) {
        _meetingDate.value = Event(location)
    }

    fun setCategory(location: String) {
        _category.value = Event(location)
    }

    fun setLanguage(location: String) {
        _language.value = Event(location)
    }

    fun setLocationSelection(boolean: Boolean) {
        _isLocationSelected.value = Event(boolean)
    }

    fun setMeetingDateSelection(boolean: Boolean) {
        _isMeetingDateSelected.value = Event(boolean)
    }

    fun setCategorySelection(boolean: Boolean) {
        _isCategorySelected.value = Event(boolean)
    }

    fun setLanguageSelection(boolean: Boolean) {
        _isLanguageSelected.value = Event(boolean)
    }

    fun createPost() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.createPost(
                    title = title.value ?: "",
                    body = body.value ?: "",
                    numberOfMember = numberOfMember.value ?: "",
                    location = _location.value?.peekContent() ?: "",
                    latitude = _locationLatitude.value?.peekContent() ?: "",
                    longitude = _locationLongitude.value?.peekContent() ?: "",
                    meetingDate = _meetingDate.value?.peekContent() ?: "",
                    category = _category.value?.peekContent() ?: "",
                    language = _language.value?.peekContent() ?: ""
                )
                if (result.isSuccessful && result.body() != null) {
                    _isLoading.value = false
                    _isCompleted.value = true
                } else {

                }
            } catch (e: HttpException) {
                // TODO
            } catch (e: Throwable) {
                // TODO
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
                && _isLocationSelected.value?.peekContent() ?: false
                && _isMeetingDateSelected.value?.peekContent() ?: false
                && _isCategorySelected.value?.peekContent() ?: false
                && _isLanguageSelected.value?.peekContent() ?: false
    }

    companion object {

        fun provideFactory(repository: PostCompositionRepository) = viewModelFactory {
            initializer {
                PostCompositionViewModel(repository)
            }
        }
    }
}
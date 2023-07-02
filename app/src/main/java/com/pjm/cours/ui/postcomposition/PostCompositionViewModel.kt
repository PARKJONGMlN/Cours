package com.pjm.cours.ui.postcomposition

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pjm.cours.util.Event

class PostCompositionViewModel : ViewModel() {

    val title = MutableLiveData<String>()
    val body = MutableLiveData<String>()
    val numberOfMember = MutableLiveData<String>()

    private val _location = MutableLiveData<Event<String>>()
    val location: LiveData<Event<String>> = _location

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
        addSource(title) { value = checkInputs()}
        addSource(body) { value = checkInputs()}
        addSource(numberOfMember) { value = checkInputs()}
        addSource(_isLocationSelected) { value = checkInputs()}
        addSource(_isMeetingDateSelected) { value = checkInputs()}
        addSource(_isCategorySelected) { value = checkInputs()}
        addSource(_isLanguageSelected) { value = checkInputs()}
    }

    fun setLocation(location: String) {
        _location.value = Event(location)
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

        fun provideFactory() = viewModelFactory {
            initializer {
                PostCompositionViewModel()
            }
        }
    }
}
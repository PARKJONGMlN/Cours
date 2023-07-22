package com.pjm.cours.ui.settinguserinfo

import android.net.Uri
import androidx.lifecycle.*
import com.google.firebase.auth.FirebaseAuth
import com.pjm.cours.data.model.User
import com.pjm.cours.data.remote.ApiResultError
import com.pjm.cours.data.remote.ApiResultException
import com.pjm.cours.data.remote.ApiResultSuccess
import com.pjm.cours.data.repository.UserRepository
import com.pjm.cours.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingUserInfoViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    private val email = FirebaseAuth.getInstance().currentUser?.email ?: ""
    val nickName = MutableLiveData<String>()
    val intro = MutableLiveData<String>()

    private val _selectedImageEvent = MutableLiveData<Event<Unit>>()
    val selectedImageEvent: LiveData<Event<Unit>> = _selectedImageEvent

    private val _selectedImageUri = MutableLiveData<Event<String>>()
    val selectedImageUri: LiveData<Event<String>> = _selectedImageUri

    val isButtonEnable = MediatorLiveData(false).apply {
        addSource(nickName) { isValidForm() }
        addSource(_selectedImageUri) { isValidForm() }
    }

    private val _isLoading = MutableLiveData<Event<Boolean>>()
    val isLoading: LiveData<Event<Boolean>> = _isLoading

    private val _isSuccess = MutableLiveData(Event(false))
    val isSuccess: LiveData<Event<Boolean>> = _isSuccess

    private val _isError = MutableLiveData(Event(false))
    val isError: LiveData<Event<Boolean>> = _isError

    private fun isValidForm() {
        isButtonEnable.value =
            !nickName.value.isNullOrEmpty() &&
                    !_selectedImageUri.value?.peekContent().isNullOrEmpty()
    }

    fun onImageSelectClick() {
        _selectedImageEvent.value = Event(Unit)
    }

    fun saveGoogleIdToken(idToken: String) {
        repository.saveGoogleIdToken(idToken)
    }

    fun setSelectedImageUri(selectedImageUri: Uri) {
        _selectedImageUri.value = Event(selectedImageUri.toString())
    }

    fun addUser() {
        viewModelScope.launch {
            _isLoading.value = Event(true)
            val result = repository.addUser(
                User(
                    selectedImageUri.value?.peekContent() ?: "",
                    nickName.value ?: "",
                    intro.value ?: "",
                    email
                )
            )
            _isLoading.value = Event(false)
            when (result) {
                is ApiResultSuccess -> {
                    repository.saveUserId(result.data["name"] ?: "")
                    _isSuccess.value = Event(true)
                }
                is ApiResultError -> {
                    _isError.value = Event(true)
                }
                is ApiResultException -> {
                    _isError.value = Event(true)
                }
            }
        }
    }
}
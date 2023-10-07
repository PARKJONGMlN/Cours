package com.pjm.cours.ui.settinguserinfo

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.pjm.cours.data.model.User
import com.pjm.cours.data.remote.ApiResultError
import com.pjm.cours.data.remote.ApiResultException
import com.pjm.cours.data.remote.ApiResultSuccess
import com.pjm.cours.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingUserInfoViewModel @Inject constructor(
    private val repository: UserRepository,
) : ViewModel() {

    private val email = FirebaseAuth.getInstance().currentUser?.email ?: ""
    private val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    val nickName = MutableStateFlow("")
    val intro = MutableStateFlow("")

    private val _selectedImageEvent = MutableSharedFlow<Unit>()
    val selectedImageEvent = _selectedImageEvent.asSharedFlow()

    private val _selectedImageUri = MutableStateFlow("")
    val selectedImageUri: StateFlow<String> = _selectedImageUri

    val isButtonEnable: StateFlow<Boolean> =
        combine(nickName, selectedImageUri) { nickName, selectedImageUri ->
            isValidForm(nickName, selectedImageUri)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false,
        )

    private fun isValidForm(nickName: String, selectedImageUri: String) =
        nickName.isNotEmpty() && selectedImageUri.isNotEmpty()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess

    private val _isError = MutableStateFlow(false)
    val isError: StateFlow<Boolean> = _isError

    fun onImageSelectClick() {
        viewModelScope.launch {
            _selectedImageEvent.emit(Unit)
        }
    }

    fun saveGoogleIdToken(idToken: String) {
        repository.saveGoogleIdToken(idToken)
    }

    fun setSelectedImageUri(selectedImageUri: Uri) {
        _selectedImageUri.value = selectedImageUri.toString()
    }

    fun addUser() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.addUser(
                uid,
                User(
                    selectedImageUri.value,
                    nickName.value,
                    intro.value,
                    email,
                ),
            )
            _isLoading.value = false
            when (result) {
                is ApiResultSuccess -> {
                    repository.saveUserId(uid)
                    _isSuccess.value = true
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
}

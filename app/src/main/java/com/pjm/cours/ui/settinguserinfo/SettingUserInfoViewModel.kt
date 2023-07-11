package com.pjm.cours.ui.settinguserinfo

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.pjm.cours.data.UserRepository
import com.pjm.cours.data.model.User
import com.pjm.cours.util.Event
import kotlinx.coroutines.launch
import retrofit2.HttpException

class SettingUserInfoViewModel(
    private val repository: UserRepository
) : ViewModel() {

    data class SettingUserInfoUiState(
        val isImageSelected: Boolean = false,
        val isNicknameEntered: Boolean = false,
        val isFormValid: Boolean = false,
        val selectedImageUri: String = ""
    )

    private val _isLoading = MutableLiveData<Event<Boolean>>()
    val isLoading: LiveData<Event<Boolean>> = _isLoading

    private val _isSuccess = MutableLiveData(Event(false))
    val isSuccess: LiveData<Event<Boolean>> = _isSuccess

    private val _isError = MutableLiveData(Event(false))
    val isError: LiveData<Event<Boolean>> = _isError

    private val _uiState = MutableLiveData(Event(SettingUserInfoUiState()))
    val uiState: LiveData<Event<SettingUserInfoUiState>> = _uiState

    private val email = FirebaseAuth.getInstance().currentUser?.email ?: ""

    fun saveGoogleIdToken(idToken: String) {
        repository.saveGoogleIdToken(idToken)
    }

    private fun checkFormValid() {
        _uiState.value = _uiState.value?.peekContent()?.let {
            Event(
                it.copy(
                    isFormValid = it.isImageSelected && it.isNicknameEntered
                )
            )
        }
    }

    fun checkNicknameEntered(isNicknameEntered: Boolean) {
        _uiState.value = _uiState.value?.peekContent()?.let {
            Event(
                it.copy(
                    isNicknameEntered = isNicknameEntered
                )
            )
        }
        checkFormValid()
    }

    fun setSelectedImageUri(selectedImageUri: Uri) {
        _uiState.value = _uiState.value?.peekContent()?.let {
            Event(
                it.copy(
                    isImageSelected = true,
                    selectedImageUri = selectedImageUri.toString(),
                )
            )
        }
        checkFormValid()
    }

    fun createUser(nickname: String, intro: String) {
        viewModelScope.launch {
            _isLoading.value = Event(true)
            try {
                val response = repository.createUser(
                    User(
                        _uiState.value?.peekContent()?.selectedImageUri ?: "",
                        nickname,
                        intro,
                        email
                    )
                )
                _isLoading.value = Event(false)
                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()
                    result?.get("name")?.let { repository.saveUserId(it) }
                    _isSuccess.value = Event(true)
                } else {

                }
            } catch (e: HttpException) {
                e.printStackTrace()
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    companion object {

        fun provideFactory(repository: UserRepository) = viewModelFactory {
            initializer {
                SettingUserInfoViewModel(repository)
            }
        }
    }
}
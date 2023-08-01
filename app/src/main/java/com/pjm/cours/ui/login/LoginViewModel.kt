package com.pjm.cours.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pjm.cours.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _isBeforeUser = MutableSharedFlow<Boolean>()
    val isBeforeUser = _isBeforeUser.asSharedFlow()

    fun getUserInfo() {
        viewModelScope.launch {
            userRepository.getUserInfo(
                onComplete = { },
                onSuccess = { },
                onError = {
                    viewModelScope.launch {
                        _isBeforeUser.emit(false)
                    }
                }
            ).collect { user ->
                _isBeforeUser.emit(true)
            }
        }
    }

    fun saveGoogleIdToken(idToken: String) {
        userRepository.saveGoogleIdToken(idToken)
    }

}
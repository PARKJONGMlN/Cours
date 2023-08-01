package com.pjm.cours.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pjm.cours.data.model.User
import com.pjm.cours.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _isError = MutableSharedFlow<Boolean>()
    val isError = _isError.asSharedFlow()

    private val _userInfo = MutableStateFlow(User())
    val userInfo: StateFlow<User> = _userInfo.asStateFlow()

    fun logOut() {
        userRepository.logOut()
    }

    fun refreshUserInfo() {
        _isLoading.value = true
        viewModelScope.launch {
            userRepository.getUserInfo(
                onComplete = {
                    _isLoading.value = false
                },
                onSuccess = {

                },
                onError = {
                    _isLoading.value = false
                    viewModelScope.launch {
                        _isError.emit(true)
                    }
                }
            ).collect { user ->
                _userInfo.value = user
            }
        }
    }

}
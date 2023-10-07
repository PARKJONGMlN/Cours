package com.pjm.cours.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.pjm.cours.data.model.User
import com.pjm.cours.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _isError = MutableSharedFlow<Boolean>()
    val isError = _isError.asSharedFlow()

    private val _isLogOutComplete = MutableSharedFlow<Boolean>()
    val isLogOutComplete = _isLogOutComplete.asSharedFlow()

    private val _isDeleteAccount = MutableSharedFlow<Boolean>()
    val isDeleteAccount = _isDeleteAccount.asSharedFlow()

    private val _userInfo = MutableStateFlow(User())
    val userInfo: StateFlow<User> = _userInfo.asStateFlow()

    fun logOut() {
        viewModelScope.launch {
            userRepository.logOut()
            FirebaseAuth.getInstance().signOut()
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            val result = userRepository.deleteAccount()
            if (result) {
                _isDeleteAccount.emit(true)
                FirebaseAuth.getInstance().currentUser?.delete()?.await()
            } else {
                _isDeleteAccount.emit(false)
            }
        }
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
                },
            ).collect { user ->
                _userInfo.value = user
            }
        }
    }
}

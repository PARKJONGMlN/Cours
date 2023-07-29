package com.pjm.cours.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pjm.cours.data.model.User
import com.pjm.cours.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _isError = MutableStateFlow(false)
    val isError = _isError.asStateFlow()

    val userInfo: StateFlow<User> = getUserInfo().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = User()
    )

    private fun getUserInfo() = userRepository.getUserInfo(
        onSuccess = { _isLoading.value = false },
        onError = {
            _isLoading.value = false
            _isError.value = true
        }
    )

    fun logOut() {
        userRepository.logOut()
    }

}
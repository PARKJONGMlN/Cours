package com.pjm.cours.ui.launcher

import androidx.lifecycle.ViewModel
import com.pjm.cours.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class LauncherViewModel @Inject constructor(
    userRepository: UserRepository
) : ViewModel() {

    private val _localGoogleIdToken:  MutableStateFlow<String> = MutableStateFlow("")
    val localGoogleIdToken: StateFlow<String> = _localGoogleIdToken

    init {
        _localGoogleIdToken.value = userRepository.getGoogleIdToken()
    }
}
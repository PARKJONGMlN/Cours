package com.pjm.cours.ui.launcher

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.pjm.cours.data.repository.UserRepository
import com.pjm.cours.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LauncherViewModel @Inject constructor(
    userRepository: UserRepository
) : ViewModel() {

    private val _localGoogleIdToken = MutableLiveData<Event<String>>()
    val localGoogleIdToken: LiveData<Event<String>> = _localGoogleIdToken

    init {
        _localGoogleIdToken.value = Event(userRepository.getGoogleIdToken())
    }
}
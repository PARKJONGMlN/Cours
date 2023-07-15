package com.pjm.cours.ui.launcher

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pjm.cours.data.repository.UserRepository
import com.pjm.cours.util.Event

class LauncherViewModel(
    userRepository: UserRepository
) : ViewModel() {

    private val _localGoogleIdToken = MutableLiveData<Event<String>>()
    val localGoogleIdToken: LiveData<Event<String>> = _localGoogleIdToken

    init {
        _localGoogleIdToken.value = Event(userRepository.getGoogleIdToken())
    }

    companion object {

        fun providerFactory(userRepository: UserRepository) = viewModelFactory {
            initializer {
                LauncherViewModel(userRepository)
            }
        }
    }
}
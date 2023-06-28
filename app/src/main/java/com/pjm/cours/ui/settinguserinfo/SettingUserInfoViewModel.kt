package com.pjm.cours.ui.settinguserinfo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pjm.cours.data.UserRepository
import com.pjm.cours.data.model.User
import com.pjm.cours.util.Event
import kotlinx.coroutines.launch
import retrofit2.HttpException

class SettingUserInfoViewModel(
    private val repository: UserRepository
) : ViewModel() {

    private val _isLoading = MutableLiveData<Event<Boolean>>()
    val isLoading: LiveData<Event<Boolean>> = _isLoading

    private fun setLoading() {
        _isLoading.value = Event(true)
    }

    fun saveGoogleIdToken(idToken: String) {
        repository.saveGoogleIdToken(idToken)
    }

    fun createUser(user: User) {
        setLoading()
        viewModelScope.launch {
            try {
                val response = repository.createPost(user)
                if (response.isSuccessful && response.body() != null) {
                    _isLoading.value = Event(false)
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
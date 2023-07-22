package com.pjm.cours.ui.postdetail

import android.util.Log
import androidx.lifecycle.*
import com.pjm.cours.data.model.Post
import com.pjm.cours.data.remote.ApiResultError
import com.pjm.cours.data.remote.ApiResultException
import com.pjm.cours.data.remote.ApiResultSuccess
import com.pjm.cours.data.repository.PostRepository
import com.pjm.cours.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostDetailViewModel @Inject constructor(private val repository: PostRepository) :
    ViewModel() {

    private val _post = MutableLiveData<Post>()
    val post: LiveData<Post> = _post

    val isButtonEnabled: LiveData<Boolean> = _post.map { post ->
        post.currentMemberCount.toInt() < post.limitMemberCount.toInt()
    }

    private val _isLoading = MutableLiveData(Event(false))
    val isLoading: LiveData<Event<Boolean>> = _isLoading

    private val _isGetPostCompleted = MutableLiveData(false)
    val isGetPostCompleted: LiveData<Boolean> = _isGetPostCompleted

    private val _isRegisterCompleted = MutableLiveData<Event<Boolean>>()
    val isRegisterCompleted: LiveData<Event<Boolean>> = _isRegisterCompleted

    private val _isError = MutableLiveData(Event(false))
    val isError: LiveData<Event<Boolean>> = _isError

    fun getPost(postId: String) {
        viewModelScope.launch {
            _isLoading.value = Event(true)
            val result = repository.getPost(postId)
            _isLoading.value = Event(false)

            when (result) {
                is ApiResultSuccess -> {
                    _post.value = result.data
                    _isGetPostCompleted.value = true
                }
                is ApiResultError -> {
                    _isError.value = Event(true)
                }
                is ApiResultException -> {
                    _isError.value = Event(true)
                }
            }
        }
    }

    fun joinMeeting(postId: String) {
        viewModelScope.launch {
            _isLoading.value = Event(true)
            val result = repository.addMember(postId, _post.value?.currentMemberCount ?: "")
            _isLoading.value = Event(false)
            when (result) {
                is ApiResultSuccess -> {
                    _isRegisterCompleted.value = Event(true)
                }
                is ApiResultError -> {
                    Log.d("TAG", "ApiResultError: code ${result.code} message ${result.message}")
                    _isRegisterCompleted.value = Event(false)
                    _isError.value = Event(true)
                }
                is ApiResultException -> {
                    Log.d("TAG", "ApiResultException: ${result.throwable.message} ")
                    _isRegisterCompleted.value = Event(false)
                    _isError.value = Event(true)
                }
            }
        }
    }
}
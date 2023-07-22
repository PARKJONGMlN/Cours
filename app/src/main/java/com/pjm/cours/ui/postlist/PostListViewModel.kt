package com.pjm.cours.ui.postlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class PostListViewModel @Inject constructor(
    private val repository: PostRepository
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isSuccess = MutableLiveData(Event(false))
    val isSuccess: LiveData<Event<Boolean>> = _isSuccess

    private val _isError = MutableLiveData(Event(false))
    val isError: LiveData<Event<Boolean>> = _isError

    private val _postList = MutableLiveData<Event<List<Post>>>()
    val postList: LiveData<Event<List<Post>>> = _postList


    init {
        getPosts()
    }

    private fun getPosts() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.getPostList()
            _isLoading.value = false
            when (result) {
                is ApiResultSuccess -> {
                    _postList.value = Event(result.data.map {
                        it.value.copy(
                            key = it.key
                        )
                    }.sortedByDescending { it.meetingDate })
                    _isSuccess.value = Event(true)
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

}
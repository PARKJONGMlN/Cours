package com.pjm.cours.ui.postlist

import android.util.Log
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

data class PostListUiState(
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val isSuccess: Boolean = false,
    val postList: List<Post> = listOf()
)

@HiltViewModel
class PostListViewModel @Inject constructor(
    private val repository: PostRepository
) : ViewModel() {

    private val _uiState = MutableLiveData(Event(PostListUiState()))
    val uiState: LiveData<Event<PostListUiState>> = _uiState

    init {
        getPosts()
    }

    private fun getPosts() {
        viewModelScope.launch {
            val result = repository.getPostList()
            when (result) {
                is ApiResultSuccess -> {
                    _uiState.value = Event(
                        PostListUiState(
                            isSuccess = true,
                            isLoading = false,
                            postList = result.data.map {
                                it.value.copy(
                                    key = it.key
                                )
                            }.sortedByDescending { it.meetingDate }
                        )
                    )
                }
                is ApiResultError -> {
                    Log.d("TAG", "ApiResultError: code ${result.code} message ${result.message}")
                }
                is ApiResultException -> {
                    Log.d("TAG", "ApiResultException: ${result.throwable.message} ")
                    _uiState.value = Event(
                        PostListUiState(
                            isSuccess = false,
                            isLoading = false,
                            isError = true
                        )
                    )
                }
            }
        }
    }
}
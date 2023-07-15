package com.pjm.cours.ui.postlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pjm.cours.data.repository.PostRepository
import com.pjm.cours.data.model.Post
import com.pjm.cours.util.Event
import kotlinx.coroutines.launch

data class PostListUiState(
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val isSuccess: Boolean = false,
    val postList: List<Post> = listOf()
)

class PostListViewModel(
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
            val postList = result.body()
            if (result.isSuccessful && postList != null) {
                _uiState.value = Event(PostListUiState(
                    isSuccess = true,
                    isLoading = false,
                    postList = postList.map {
                        it.value.copy(
                            key = it.key
                        )
                    }.sortedByDescending { it.meetingDate }
                )

                )

            }
        }
    }

    companion object {

        fun provideFactory(repository: PostRepository) = viewModelFactory {
            initializer {
                PostListViewModel(repository)
            }
        }
    }
}
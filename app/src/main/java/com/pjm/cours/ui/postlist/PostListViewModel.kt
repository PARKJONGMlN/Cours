package com.pjm.cours.ui.postlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pjm.cours.data.model.Post
import com.pjm.cours.data.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class PostListViewModel @Inject constructor(
    private val repository: PostRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _isError = MutableStateFlow(false)
    val isError = _isError.asStateFlow()

    val postList: StateFlow<List<Post>> = getPosts().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private fun getPosts() = repository.getPostList(
        onSuccess = {
            _isLoading.value = false
        },
        onError = {
            _isLoading.value = false
            _isError.value = true
        },
    )

}
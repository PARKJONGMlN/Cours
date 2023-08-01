package com.pjm.cours.ui.postlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pjm.cours.data.model.Post
import com.pjm.cours.data.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostListViewModel @Inject constructor(
    private val repository: PostRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _isError = MutableSharedFlow<Boolean>()
    val isError = _isError.asSharedFlow()

    private val _postList = MutableStateFlow<List<Post>>(emptyList())
    val postList: StateFlow<List<Post>> = _postList.asStateFlow()
    fun refreshPostList() {
        _isLoading.value = true
        viewModelScope.launch {
            repository.getPostList(
                onSuccess = {
                    _isLoading.value = false
                },
                onError = {
                    _isLoading.value = false
                    viewModelScope.launch {
                        _isError.emit(true)
                    }
                }
            ).collect { postList ->
                _postList.value = postList
            }
        }
    }

}
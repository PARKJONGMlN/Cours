package com.pjm.cours.ui.postdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pjm.cours.data.local.entities.ChatPreviewEntity
import com.pjm.cours.data.model.Post
import com.pjm.cours.data.remote.ApiResultError
import com.pjm.cours.data.remote.ApiResultException
import com.pjm.cours.data.remote.ApiResultSuccess
import com.pjm.cours.data.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostDetailViewModel @Inject constructor(private val repository: PostRepository) :
    ViewModel() {

    private val _post = MutableStateFlow(Post())
    val post = _post.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isGetPostCompleted = MutableStateFlow(false)
    val isGetPostCompleted = _isGetPostCompleted.asStateFlow()

    private val _isRegisterCompleted = MutableStateFlow(false)
    val isRegisterCompleted = _isRegisterCompleted.asStateFlow()

    private val _isError = MutableStateFlow(false)
    val isError = _isError.asStateFlow()

    private val chatPreviewList: StateFlow<List<ChatPreviewEntity>> =
        repository.getChatPreviewList().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val isButtonEnabled: StateFlow<Boolean> = combineIsButtonEnable().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    private fun combineIsButtonEnable() = combine(_post, chatPreviewList) { post, chatPreviewList ->
        if (post.currentMemberCount.isNotEmpty()) {
            val isPostMember = chatPreviewList.any {
                it.postId == post.key
            }
            !isPostMember && post.currentMemberCount.toInt() < post.limitMemberCount.toInt()
        } else {
            false
        }
    }

    fun getPost(postId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.getPost(postId)
            _isLoading.value = false

            when (result) {
                is ApiResultSuccess -> {
                    _post.value = result.data.copy(
                        key = postId
                    )
                    _isGetPostCompleted.value = true
                }
                is ApiResultError -> {
                    _isError.value = true
                }
                is ApiResultException -> {
                    _isError.value = true
                }
            }
        }
    }

    fun joinMeeting(postId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.addMember(postId, _post.value.currentMemberCount)
            _isLoading.value = false
            when (result) {
                is ApiResultSuccess -> {
                    _isRegisterCompleted.value = true
                }
                is ApiResultError -> {
                    _isRegisterCompleted.value = false
                    _isError.value = true
                }
                is ApiResultException -> {
                    _isRegisterCompleted.value = false
                    _isError.value = true
                }
            }
        }
    }

}
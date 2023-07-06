package com.pjm.cours.ui.postdetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pjm.cours.data.PostRepository
import com.pjm.cours.data.model.Post
import com.pjm.cours.util.Event
import kotlinx.coroutines.launch

class PostDetailViewModel(private val repository: PostRepository) : ViewModel() {

    private val _post = MutableLiveData<Post>()
    val post: LiveData<Post> = _post

    private val _isGetPostCompleted = MutableLiveData(Event(false))
    val isGetPostCompleted: LiveData<Event<Boolean>> = _isGetPostCompleted

    private val _isRegisterCompleted = MutableLiveData(Event(false))
    val isRegisterCompleted: LiveData<Event<Boolean>> = _isRegisterCompleted

    fun getPost(postId: String) {
        viewModelScope.launch {
            val result = repository.getPost(postId)
            val resultPost = result.body()
            resultPost?.let { post ->
                _post.value = post
                _isGetPostCompleted.value = Event(true)
            }
        }
    }

    fun joinMeeting(postId: String) {
        viewModelScope.launch {
            val result = repository.registerMember(postId, _post.value?.currentMemberCount ?: "")
            result.body()?.let {
                _isRegisterCompleted.value = Event(true)
            }
        }
    }

    companion object {

        fun provideFactory(repository: PostRepository) = viewModelFactory {
            initializer {
                PostDetailViewModel(repository)
            }
        }
    }
}
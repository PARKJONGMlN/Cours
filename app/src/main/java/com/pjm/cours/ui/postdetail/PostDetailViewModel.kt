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

    private val _isCompleted = MutableLiveData(Event(false))
    val isCompleted: LiveData<Event<Boolean>> = _isCompleted

    fun getPost(postId: String) {
        viewModelScope.launch {
            val result = repository.getPost(postId)
            val resultPost = result.body()
            resultPost?.let { post ->
                _post.value = post
                _isCompleted.value = Event(true)
            }
        }
    }

    fun joinMeeting(postId: String) {
        viewModelScope.launch {
            repository.registerMember(postId,_post.value?.currentMemberCount ?: "")
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
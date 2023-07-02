package com.pjm.cours.ui.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pjm.cours.data.PostRepository
import com.pjm.cours.data.model.Post
import kotlinx.coroutines.launch

class MapViewModel(
    private val repository: PostRepository
) : ViewModel() {

    private val _isCompleted = MutableLiveData(false)
    val isCompleted: LiveData<Boolean> = _isCompleted

    var postList: List<Post>? = listOf()
    fun getPosts() {
        viewModelScope.launch {
            val result = repository.getPostList()
            postList = result.body()?.values?.toList()
            _isCompleted.value = true
        }
    }

    companion object {

        fun provideFactory(repository: PostRepository) = viewModelFactory {
            initializer {
                MapViewModel(repository)
            }
        }
    }
}
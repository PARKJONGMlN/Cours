package com.pjm.cours.ui.postlist

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.pjm.cours.R
import com.pjm.cours.databinding.FragmentPostListBinding
import com.pjm.cours.ui.BaseFragment
import com.pjm.cours.ui.MainActivity
import com.pjm.cours.ui.postcomposition.PostCompositionActivity
import com.pjm.cours.ui.postdetail.PostDetailActivity
import com.pjm.cours.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PostListFragment : BaseFragment<FragmentPostListBinding>(R.layout.fragment_post_list) {

    private val viewModel: PostListViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        setLayout()
    }

    private fun setLayout() {
        setAppBar()
        setPostList()
        setErrorMessage()
    }

    private fun setAppBar() {
        binding.appBarPostList.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.create_post -> {
                    startActivity(Intent(requireContext(), PostCompositionActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun setPostList() {
        val adapter = PostListAdapter { post ->
            val intent = Intent(requireContext(), PostDetailActivity::class.java)
            intent.putExtra(Constants.POST_ID, post.key)
            startActivity(intent)
        }
        binding.recyclerViewPostList.adapter = adapter
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.postList.flowWithLifecycle(
                viewLifecycleOwner.lifecycle,
                Lifecycle.State.STARTED
            ).collect { postList ->
                if (postList.isNotEmpty()) {
                    adapter.submitList(postList)
                }
            }
        }
    }

    private fun setErrorMessage() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isError.flowWithLifecycle(
                viewLifecycleOwner.lifecycle,
                Lifecycle.State.STARTED
            ).collect { isError ->
                if (isError) {
                    (requireActivity() as MainActivity).showSnackBar(getString(R.string.error_message))
                }
            }
        }
    }

}
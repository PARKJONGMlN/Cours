package com.pjm.cours.ui.postlist

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.pjm.cours.R
import com.pjm.cours.databinding.FragmentPostListBinding
import com.pjm.cours.ui.BaseFragment
import com.pjm.cours.ui.postcomposition.PostCompositionActivity
import com.pjm.cours.ui.postdetail.PostDetailActivity
import com.pjm.cours.util.Constants
import com.pjm.cours.util.EventObserver
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PostListFragment : BaseFragment<FragmentPostListBinding>(R.layout.fragment_post_list) {

    private val viewModel: PostListViewModel by viewModels()

    private lateinit var adapter: PostListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setLayout()
        setObserver()
    }

    private fun setLayout() {
        binding.appBarPostList.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.create_post -> {
                    startActivity(Intent(requireContext(), PostCompositionActivity::class.java))
                    true
                }
                else -> false
            }
        }
        adapter = PostListAdapter { post ->
            val intent = Intent(requireContext(), PostDetailActivity::class.java)
            intent.putExtra(Constants.POST_ID, post.key)
            startActivity(intent)
        }
        binding.recyclerViewPostList.adapter = adapter
    }

    private fun setObserver() {
        viewModel.uiState.observe(viewLifecycleOwner, EventObserver { uiState ->
            if (uiState.isSuccess) {
                adapter.submitList(uiState.postList)
            }

            if (uiState.isLoading) {
                binding.progressLoading.visibility = View.VISIBLE
            } else {
                binding.progressLoading.visibility = View.INVISIBLE
            }
        })
    }

}
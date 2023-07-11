package com.pjm.cours.ui.postlist

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.pjm.cours.R
import com.pjm.cours.databinding.FragmentPostListBinding
import com.pjm.cours.ui.BaseFragment
import com.pjm.cours.ui.postcomposition.PostCompositionActivity

class PostListFragment : BaseFragment<FragmentPostListBinding>(R.layout.fragment_post_list) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setLayout()
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
    }

}
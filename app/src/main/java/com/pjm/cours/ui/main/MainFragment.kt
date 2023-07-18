package com.pjm.cours.ui.main

import android.os.Bundle
import android.view.View
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.google.android.material.snackbar.Snackbar
import com.pjm.cours.R
import com.pjm.cours.databinding.FragmentMainBinding
import com.pjm.cours.ui.BaseFragment
import com.pjm.cours.ui.chatlist.ChatListFragment
import com.pjm.cours.ui.map.MapFragment
import com.pjm.cours.ui.postlist.PostListFragment
import com.pjm.cours.ui.profile.ProfileFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainFragment : BaseFragment<FragmentMainBinding>(R.layout.fragment_main) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setLayout()
    }

    private fun setLayout() {
        binding.bottomNavigation.selectedItemId = R.id.item_chat
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.item_map -> {
                    childFragmentManager.commit {
                        setReorderingAllowed(true)
                        addToBackStack(null)
                        replace<MapFragment>(R.id.fragment_container_view_main)
                    }
                    true
                }
                R.id.item_list -> {
                    childFragmentManager.commit {
                        setReorderingAllowed(true)
                        addToBackStack(null)
                        replace<PostListFragment>(R.id.fragment_container_view_main)
                    }
                    true
                }
                R.id.item_chat -> {
                    childFragmentManager.commit {
                        setReorderingAllowed(true)
                        addToBackStack(null)
                        replace<ChatListFragment>(R.id.fragment_container_view_main)
                    }
                    true
                }
                R.id.item_profile -> {
                    childFragmentManager.commit {
                        setReorderingAllowed(true)
                        addToBackStack(null)
                        replace<ProfileFragment>(R.id.fragment_container_view_main)
                    }
                    true
                }
                else -> false
            }
        }
    }

    fun showSnackBar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
            .setAnchorView(binding.bottomNavigation)
            .show()
    }

    companion object {
        const val TAG = "MainFragment"
    }
}
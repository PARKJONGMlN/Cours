package com.pjm.cours.ui.profile

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.pjm.cours.R
import com.pjm.cours.databinding.FragmentProfileBinding
import com.pjm.cours.ui.BaseFragment
import com.pjm.cours.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : BaseFragment<FragmentProfileBinding>(R.layout.fragment_profile) {

    private val viewModel: ProfileViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        setLayout()
    }

    private fun setLayout() {
        setErrorMessage()
        setAppBar()
    }

    private fun setAppBar() {
        binding.appBarProfile.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_setting -> {
                    viewModel.logOut()
                    val action = ProfileFragmentDirections.actionProfileFragmentToLoginFragment()
                    findNavController().navigate(action)
                    true
                }
                else -> false
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
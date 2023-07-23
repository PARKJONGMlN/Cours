package com.pjm.cours.ui.launcher

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LauncherFragment : Fragment() {

    private val viewModel: LauncherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setObserver()
    }

    private fun setObserver() {
        lifecycleScope.launch {
            viewModel.localGoogleIdToken.flowWithLifecycle(
                lifecycle,
                Lifecycle.State.STARTED
            ).collect { localGoogleIdToken ->
                if (localGoogleIdToken.isEmpty()) {
                    findNavController().navigate(LauncherFragmentDirections.actionLauncherFragmentToLoginFragment())
                } else {
                    findNavController().navigate(LauncherFragmentDirections.actionLauncherFragmentToChatListFragment())
                }
            }
        }
    }

}
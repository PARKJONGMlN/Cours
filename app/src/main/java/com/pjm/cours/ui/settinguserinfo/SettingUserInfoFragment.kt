package com.pjm.cours.ui.settinguserinfo

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.pjm.cours.R
import com.pjm.cours.databinding.FragmentSettingUserInfoBinding
import com.pjm.cours.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingUserInfoFragment :
    BaseFragment<FragmentSettingUserInfoBinding>(R.layout.fragment_setting_user_info) {

    private val viewModel: SettingUserInfoViewModel by viewModels()
    private val getContent = getActivityResultLauncher()
    private val args: SettingUserInfoFragmentArgs by navArgs()

    private fun getActivityResultLauncher() =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { imageUri ->
                viewModel.setSelectedImageUri(imageUri)
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setLayout()
    }

    private fun setLayout() {
        binding.viewModel = viewModel
        setImageSelectEvent()
        setAddUserComplete()
        setErrorMessage()
    }

    private fun setImageSelectEvent() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.selectedImageEvent.flowWithLifecycle(
                viewLifecycleOwner.lifecycle,
                Lifecycle.State.STARTED,
            ).collect {
                getContent.launch("image/*")
            }
        }
    }

    private fun setAddUserComplete() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isSuccess.flowWithLifecycle(
                viewLifecycleOwner.lifecycle,
                Lifecycle.State.STARTED,
            ).collect { isSuccess ->
                if (isSuccess) {
                    viewModel.saveGoogleIdToken(args.uid)
                    findNavController().navigate(SettingUserInfoFragmentDirections.actionSettingUserInfoFragmentToChatListFragment())
                }
            }
        }
    }

    private fun setErrorMessage() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isError.flowWithLifecycle(
                viewLifecycleOwner.lifecycle,
                Lifecycle.State.STARTED,
            ).collect { isError ->
                if (isError) {
                    Snackbar.make(
                        binding.root,
                        getString(R.string.error_message),
                        Snackbar.LENGTH_SHORT,
                    )
                        .setAnchorView(binding.btnSettingComplete)
                        .show()
                }
            }
        }
    }
}

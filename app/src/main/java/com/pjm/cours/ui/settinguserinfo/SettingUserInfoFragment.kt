package com.pjm.cours.ui.settinguserinfo

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import com.pjm.cours.R
import com.pjm.cours.databinding.FragmentSettingUserInfoBinding
import com.pjm.cours.ui.BaseFragment
import com.pjm.cours.ui.common.ProgressDialogFragment
import com.pjm.cours.ui.main.MainFragment
import com.pjm.cours.util.Constants
import com.pjm.cours.util.EventObserver
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingUserInfoFragment :
    BaseFragment<FragmentSettingUserInfoBinding>(R.layout.fragment_setting_user_info) {

    private val viewModel: SettingUserInfoViewModel by viewModels()
    private val getContent = getActivityResultLauncher()
    private val dialogLoading = ProgressDialogFragment()

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
        setProgressDialog()
        setErrorMessage()
    }

    private fun setImageSelectEvent() {
        viewModel.selectedImageEvent.observe(viewLifecycleOwner, EventObserver {
            getContent.launch("image/*")
        })
    }

    private fun setAddUserComplete() {
        viewModel.isSuccess.observe(viewLifecycleOwner, EventObserver { isSuccess ->
            if (isSuccess) {
                arguments?.getString(Constants.KEY_GOOGLE_ID_TOKEN)
                    ?.let {
                        viewModel.saveGoogleIdToken(it)
                        parentFragmentManager.popBackStack()
                        parentFragmentManager.commit {
                            replace<MainFragment>(R.id.fragment_container_view)
                        }
                    }
            }
        })
    }

    private fun setErrorMessage() {
        viewModel.isError.observe(viewLifecycleOwner, EventObserver { isError ->
            if (isError) {
                dialogLoading.dismiss()
                Snackbar.make(
                    binding.root,
                    getString(R.string.error_message),
                    Snackbar.LENGTH_SHORT
                )
                    .setAnchorView(binding.btnSettingComplete)
                    .show()
            }
        })
    }

    private fun setProgressDialog() {
        viewModel.isLoading.observe(viewLifecycleOwner, EventObserver { isLoading ->
            if (isLoading) {
                dialogLoading.show(parentFragmentManager, Constants.DIALOG_FRAGMENT_PROGRESS_TAG)
            } else {
                dialogLoading.dismiss()
            }
        })
    }

}
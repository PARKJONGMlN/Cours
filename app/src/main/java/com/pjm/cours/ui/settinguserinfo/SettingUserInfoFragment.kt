package com.pjm.cours.ui.settinguserinfo

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.fragment.app.viewModels
import coil.load
import coil.transform.CircleCropTransformation
import com.pjm.cours.CoursApplication
import com.pjm.cours.R
import com.pjm.cours.data.repository.UserRepository
import com.pjm.cours.databinding.FragmentSettingUserInfoBinding
import com.pjm.cours.ui.BaseFragment
import com.pjm.cours.ui.common.ProgressDialogFragment
import com.pjm.cours.ui.main.MainFragment
import com.pjm.cours.util.Constants
import com.pjm.cours.util.EventObserver

class SettingUserInfoFragment :
    BaseFragment<FragmentSettingUserInfoBinding>(R.layout.fragment_setting_user_info) {

    private val viewModel: SettingUserInfoViewModel by viewModels {
        SettingUserInfoViewModel.provideFactory(
            UserRepository(
                CoursApplication.apiContainer.provideApiClient(),
                CoursApplication.preferencesManager
            )
        )
    }

    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                binding.ivUserProfileImage.load(it) {
                    transformations(CircleCropTransformation())
                }
                viewModel.setSelectedImageUri(it)
            }
        }

    private val dialogLoading = ProgressDialogFragment()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setLayout()
        setObserver()
    }

    private fun setLayout() {
        binding.ivUserProfileImage.setOnClickListener {
            getContent.launch("image/*")
        }
        binding.etUserNickname.doOnTextChanged { _, _, _, _ ->
            viewModel.checkNicknameEntered(binding.etUserNickname.text.toString().isNotBlank())
        }
        binding.btnSettingComplete.setOnClickListener {
            viewModel.createUser(
                binding.etUserNickname.text.toString(),
                binding.etUserNickname.text.toString()
            )
        }
    }

    private fun setObserver() {
        viewModel.uiState.observe(viewLifecycleOwner, EventObserver { uiState ->
            binding.btnSettingComplete.isEnabled = uiState.isFormValid
        })
        viewModel.isLoading.observe(viewLifecycleOwner, EventObserver { isLoading ->
            if (isLoading) {
                dialogLoading.show(parentFragmentManager, Constants.DIALOG_FRAGMENT_PROGRESS_TAG)
            } else {
                dialogLoading.dismiss()
            }
        })
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

}
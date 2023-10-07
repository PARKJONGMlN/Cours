package com.pjm.cours.ui.postdetail

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.pjm.cours.R
import com.pjm.cours.databinding.ActivityPostDetailBinding
import com.pjm.cours.ui.chat.ChatActivity
import com.pjm.cours.ui.common.ProgressDialogFragment
import com.pjm.cours.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PostDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostDetailBinding
    private val viewModel: PostDetailViewModel by viewModels()
    private lateinit var postId: String
    private lateinit var distance: String
    private lateinit var dialogLoading: ProgressDialogFragment
    private lateinit var dialogCheckRegister: CheckRegisterDialogFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        postId = intent.getStringExtra(Constants.POST_ID) ?: ""
        distance = intent.getStringExtra(Constants.POST_DISTANCE) ?: ""

        binding.distance = distance
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        initUiState()
        setDialog()
        setLayout()
        setRegisterComplete()
        setLoading()
        setErrorMessage()
    }

    private fun initUiState() {
        viewModel.getPost(postId)
    }

    private fun setDialog() {
        dialogLoading = ProgressDialogFragment()
        dialogCheckRegister = CheckRegisterDialogFragment {
            viewModel.joinMeeting(postId)
        }
    }

    private fun setLayout() {
        binding.btnSettingComplete.setOnClickListener {
            if (!dialogCheckRegister.isAdded) {
                dialogCheckRegister.show(
                    supportFragmentManager,
                    Constants.DIALOG_FRAGMENT_CHECK_REGISTER_TAG,
                )
            }
        }
        binding.appBarPostDeatil.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setErrorMessage() {
        lifecycleScope.launch {
            viewModel.isError.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collect { isError ->
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

    private fun setLoading() {
        lifecycleScope.launch {
            viewModel.isLoading.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collect { isLoading ->
                    if (isLoading) {
                        dialogLoading.show(
                            supportFragmentManager,
                            Constants.DIALOG_FRAGMENT_PROGRESS_TAG,
                        )
                    } else {
                        if (dialogLoading.isAdded) {
                            dialogLoading.dismiss()
                        }
                    }
                }
        }
    }

    private fun setRegisterComplete() {
        lifecycleScope.launch {
            viewModel.isRegisterCompleted.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collect { isRegisterCompleted ->
                    if (isRegisterCompleted) {
                        dialogCheckRegister.dismiss()
                        intent = Intent(this@PostDetailActivity, ChatActivity::class.java)
                        intent.putExtra(Constants.POST_ID, postId)
                        startActivity(intent)
                        finish()
                    } else {
                        if (dialogCheckRegister.isAdded) {
                            dialogCheckRegister.dismiss()
                        }
                    }
                }
        }
    }
}

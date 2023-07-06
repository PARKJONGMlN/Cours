package com.pjm.cours.ui.postdetail

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.pjm.cours.CoursApplication
import com.pjm.cours.R
import com.pjm.cours.data.PostRepository
import com.pjm.cours.databinding.ActivityPostDetailBinding
import com.pjm.cours.ui.chat.ChatActivity
import com.pjm.cours.ui.common.ProgressDialogFragment
import com.pjm.cours.util.Constants
import com.pjm.cours.util.EventObserver

class PostDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostDetailBinding
    private val viewModel: PostDetailViewModel by viewModels {
        PostDetailViewModel.provideFactory(
            PostRepository(
                CoursApplication.apiContainer.provideApiClient(),
                CoursApplication.preferencesManager
            )
        )
    }
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

        initUiState()
        setDialog()
        setLayout()
        setObserver()
    }

    private fun initUiState() {
        viewModel.getPost(postId)
    }

    private fun setDialog(){
        dialogLoading = ProgressDialogFragment()
        dialogCheckRegister = CheckRegisterDialogFragment{
            viewModel.joinMeeting(postId)
        }
    }

    private fun setLayout() {
        binding.btnSettingComplete.setOnClickListener {
            dialogCheckRegister.show(supportFragmentManager, Constants.DIALOG_FRAGMENT_CHECK_REGISTER_TAG)
        }
        binding.appBarPostDeatil.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setObserver() {
        viewModel.isGetPostCompleted.observe(this, EventObserver { isGetPostCompleted ->
            if (isGetPostCompleted) {
                binding.tvTitlePostDetail.text = viewModel.post.value?.title
                binding.tvCategoryPostDetail.text = viewModel.post.value?.category
                binding.tvLanguagePostDetail.text = viewModel.post.value?.language
                binding.tvDistancePostDetail.text =
                    getString(R.string.format_post_distance_m, distance)
                binding.tvBodyPostDetail.text = viewModel.post.value?.body
                val limitMemberCount = viewModel.post.value?.limitMemberCount ?: ""
                val currentMemberCount = viewModel.post.value?.currentMemberCount ?: ""
                binding.tvCurrentPeoplePostDetail.text = getString(
                    R.string.format_post_member_count,
                    currentMemberCount,
                    limitMemberCount
                )
                binding.tvMeetingDatePostDetail.text = viewModel.post.value?.meetingDate
                binding.nestedScrollViewPostDetail.visibility = View.VISIBLE

                if (currentMemberCount.toInt() < limitMemberCount.toInt()) {
                    binding.btnSettingComplete.isEnabled = true
                }
            }
        })

        viewModel.isRegisterCompleted.observe(this, EventObserver { isRegisterCompleted ->
            if (isRegisterCompleted) {
                dialogCheckRegister.dismiss()
                intent = Intent(this, ChatActivity::class.java)
                intent.putExtra(Constants.POST_ID, postId)
                startActivity(intent)
                finish()
            }
        })

        viewModel.isLoading.observe(this, EventObserver { isLoading ->
            if (isLoading) {
                dialogLoading.show(supportFragmentManager, Constants.DIALOG_FRAGMENT_PROGRESS_TAG)
            } else {
                dialogLoading.dismiss()
            }
        })
    }

    companion object {
        const val TAG = "PostDetailActivity"
    }
}

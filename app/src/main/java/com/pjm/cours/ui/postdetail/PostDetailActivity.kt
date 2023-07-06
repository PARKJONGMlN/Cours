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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        postId = intent.getStringExtra(Constants.POST_ID) ?: ""
        distance = intent.getStringExtra(Constants.POST_DISTANCE) ?: ""

        setLayout()
        setObserver()

        viewModel.getPost(postId)
    }

    private fun setLayout() {
        binding.btnSettingComplete.setOnClickListener {
            viewModel.joinMeeting(postId)
            intent = Intent(this, ChatActivity::class.java)
            intent.putExtra(Constants.POST_ID, postId)
            startActivity(intent)
        }
        binding.appBarPostDeatil.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setObserver() {
        viewModel.isCompleted.observe(this, EventObserver { isCompleted ->
            if (isCompleted) {
                binding.tvTitlePostDetail.text = viewModel.post.value?.title
                binding.tvCategoryPostDetail.text = viewModel.post.value?.category
                binding.tvLanguagePostDetail.text = viewModel.post.value?.language
                binding.tvDistancePostDetail.text =
                    getString(R.string.format_post_distance_m, distance)
                binding.tvBodyPostDetail.text = viewModel.post.value?.body
                val limitMemberCount = viewModel.post.value?.limitMemberCount
                val currentMemberCount = viewModel.post.value?.currentMemberCount
                binding.tvCurrentPeoplePostDetail.text = "$limitMemberCount / $currentMemberCount"
                binding.tvMeetingDatePostDetail.text = viewModel.post.value?.meetingDate
                binding.nestedScrollViewPostDetail.visibility = View.VISIBLE
            }
        })
    }

    companion object {
        const val TAG = "PostDetailActivity"
    }
}

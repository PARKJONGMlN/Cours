package com.pjm.cours.ui.postdetail

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.pjm.cours.databinding.ActivityPostDetailBinding

class PostDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    companion object {
        const val TAG = "PostDetailActivity"
    }
}

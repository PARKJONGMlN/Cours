package com.pjm.cours.ui.postcomposition

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.pjm.cours.databinding.ActivityPostCompositionBinding

class PostCompositionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostCompositionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostCompositionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setLayout()
    }

    private fun setLayout() {
        binding.appBarPostComposition.setNavigationOnClickListener {
            finish()
        }
    }
}
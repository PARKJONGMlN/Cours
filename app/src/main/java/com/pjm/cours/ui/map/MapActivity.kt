package com.pjm.cours.ui.map

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.pjm.cours.R
import com.pjm.cours.databinding.ActivityMapBinding
import com.pjm.cours.ui.postcomposition.PostCompositionActivity

class MapActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMapBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setLayout()
    }

    private fun setLayout() {
        binding.appBarPostComposition.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.create_post -> {
                    startActivity(Intent(this,PostCompositionActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
}
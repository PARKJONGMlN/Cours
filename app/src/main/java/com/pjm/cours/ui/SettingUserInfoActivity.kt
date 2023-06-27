package com.pjm.cours.ui

import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import coil.load
import coil.transform.CircleCropTransformation
import com.pjm.cours.databinding.ActivitySettingUserInfoBinding

class SettingUserInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingUserInfoBinding
    private var isImageSelected = false
    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri.let {
                binding.ivUserProfileImage.load(uri) {
                    transformations(CircleCropTransformation())
                }
                isImageSelected = true
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingUserInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivUserProfileImage.setOnClickListener {
            getContent.launch("image/*")
        }
    }
}
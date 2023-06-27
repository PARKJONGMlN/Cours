package com.pjm.cours.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import coil.load
import coil.transform.CircleCropTransformation
import com.pjm.cours.databinding.ActivitySettingUserInfoBinding

class SettingUserInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingUserInfoBinding
    private var isImageSelected = false
    private var isNicknameEntered = false
    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri.let {
                binding.ivUserProfileImage.load(uri) {
                    transformations(CircleCropTransformation())
                }
                isImageSelected = true
                checkInputs()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingUserInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSettingComplete.setOnClickListener{
            startActivity(Intent(this, MapActivity::class.java))
        }

        binding.ivUserProfileImage.setOnClickListener {
            getContent.launch("image/*")
            Log.d("SettingUserInfoActivity", "onCreate: ")
        }

        binding.etUserNickname.doOnTextChanged { _, _, _, _ ->
            isNicknameEntered = binding.etUserNickname.text.toString().isNotBlank()
            checkInputs()
        }
    }

    private fun checkInputs() {
        binding.btnSettingComplete.isEnabled = isImageSelected && isNicknameEntered
    }
}
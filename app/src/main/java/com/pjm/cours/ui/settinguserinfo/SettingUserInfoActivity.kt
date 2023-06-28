package com.pjm.cours.ui.settinguserinfo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import coil.load
import coil.transform.CircleCropTransformation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.pjm.cours.CoursApplication
import com.pjm.cours.data.model.User
import com.pjm.cours.databinding.ActivitySettingUserInfoBinding
import com.pjm.cours.ui.MapActivity
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SettingUserInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingUserInfoBinding
    private var isImageSelected = false
    private var isNicknameEntered = false
    private lateinit var selectedImageUri: Uri
    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                binding.ivUserProfileImage.load(uri) {
                    transformations(CircleCropTransformation())
                }
                selectedImageUri = it
                isImageSelected = true
                checkInputs()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingUserInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSettingComplete.setOnClickListener {
            lifecycleScope.launch {
                val storageRef = FirebaseStorage.getInstance().reference
                val location =
                    "image/${FirebaseAuth.getInstance().currentUser?.email.toString()}_${System.currentTimeMillis()}"
                val imageRef = storageRef.child(location)
                imageRef.putFile(selectedImageUri).await()
                val idToken =
                    FirebaseAuth.getInstance().currentUser?.getIdToken(true)?.await()?.token
                Log.d(
                    "SettingUserInfoActivity",
                    "Response: ${
                        runCatching {
                            CoursApplication.apiContainer.provideApiClient()
                                .createUser(
                                    idToken,
                                    User(
                                        location,
                                        binding.etUserIntro.text.toString(),
                                        binding.etUserIntro.text.toString(),
                                        FirebaseAuth.getInstance().currentUser?.email.toString()
                                    )
                                )
                        }.onSuccess {
                            startActivity(
                                Intent(
                                    this@SettingUserInfoActivity,
                                    MapActivity::class.java
                                )
                            )
                        }.onFailure {
                            Log.d("runCatching", "Throwable: $it")
                        }
                    }"
                )
            }
        }

        binding.ivUserProfileImage.setOnClickListener {
            getContent.launch("image/*")
            Log.d("SettingUserInfoActivity", "onCreate: ")
        }

        binding.etUserNickname.doOnTextChanged { _, _, _, _ ->
            isNicknameEntered = binding.etUserNickname.text.toString().isNotBlank()
            checkInputs()
        }

        binding.etUserIntro.addTextChangedListener(object : TextWatcher {
            var maxText = ""
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                maxText = p0.toString()
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (binding.etUserIntro.lineCount > 3) {
                    binding.etUserIntro.setText(maxText)
                    binding.etUserIntro.setSelection(binding.etUserIntro.length())
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })
    }

    private fun checkInputs() {
        binding.btnSettingComplete.isEnabled = isImageSelected && isNicknameEntered
    }
}
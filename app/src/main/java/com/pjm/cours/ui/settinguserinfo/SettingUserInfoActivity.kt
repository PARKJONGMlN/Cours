package com.pjm.cours.ui.settinguserinfo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import coil.load
import coil.transform.CircleCropTransformation
import com.google.firebase.auth.FirebaseAuth
import com.pjm.cours.CoursApplication
import com.pjm.cours.data.UserRepository
import com.pjm.cours.data.model.User
import com.pjm.cours.databinding.ActivitySettingUserInfoBinding
import com.pjm.cours.ui.map.MapActivity
import com.pjm.cours.util.Constants

class SettingUserInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingUserInfoBinding
    private val viewModel: SettingUserInfoViewModel by viewModels {
        SettingUserInfoViewModel.provideFactory(
            UserRepository(
                CoursApplication.apiContainer.provideApiClient(),
                CoursApplication.preferencesManager
            )
        )
    }
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
        setLayout()
        setObserver()
    }

    private fun setLayout() {
        binding.btnSettingComplete.setOnClickListener {
            viewModel.createUser(
                User(
                    selectedImageUri.toString(),
                    binding.etUserNickname.text.toString(),
                    binding.etUserIntro.text.toString(),
                    FirebaseAuth.getInstance().currentUser?.email.toString()
                )
            )
        }

        binding.ivUserProfileImage.setOnClickListener {
            getContent.launch("image/*")
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

    private fun setObserver() {
        viewModel.isLoading.observe(this) {
            if (it.peekContent()) {
                binding.groupLoading.visibility = View.VISIBLE
                disableScreenTouch()
            } else {
                intent.getStringExtra(Constants.KEY_GOOGLE_ID_TOKEN)
                    ?.let { idToken ->
                        viewModel.saveGoogleIdToken(idToken)
                        binding.groupLoading.visibility = View.GONE
                        startActivity(Intent(this, MapActivity::class.java))
                    }
            }
        }
    }

    private fun checkInputs() {
        binding.btnSettingComplete.isEnabled = isImageSelected && isNicknameEntered
    }

    private fun disableScreenTouch() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }
}
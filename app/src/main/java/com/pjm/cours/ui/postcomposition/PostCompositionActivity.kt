package com.pjm.cours.ui.postcomposition

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.google.android.material.datepicker.MaterialDatePicker
import com.pjm.cours.R
import com.pjm.cours.data.ItemStorage
import com.pjm.cours.databinding.ActivityPostCompositionBinding
import com.pjm.cours.ui.location.LocationActivity
import com.pjm.cours.util.Constants
import com.pjm.cours.util.DateFormat

class PostCompositionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostCompositionBinding
    private var isLocationSelected = false
    private var isMeetingDateSelected = false
    private var isCategorySelected = false
    private var isLanguageSelected = false
    private val startForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            val location = result.data?.getStringExtra(Constants.SELECTED_LOCATION)!!
            binding.tvPostSelectedLocation.text = location
            isLocationSelected = true
            checkInputs()
        }
    }

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

        binding.etPostTitle.doOnTextChanged { _, _, _, _ ->
            checkInputs()
        }

        binding.etPostBody.doOnTextChanged { _, _, _, _ ->
            checkInputs()
        }

        binding.etPostNumberOfMember.doOnTextChanged { _, _, _, _ ->
            checkInputs()
        }

        binding.ivSelectDateIcon.setOnClickListener {
            val datePicker =
                MaterialDatePicker.Builder.datePicker()
                    .setTitleText(R.string.label_post_select_date_message)
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build()
            datePicker.addOnPositiveButtonClickListener {
                binding.tvPostSelectedDate.text = DateFormat.convertDisplayDate(it)
                isMeetingDateSelected = true
                checkInputs()
            }
            datePicker.show(supportFragmentManager, "SELECT_DATE")
        }

        binding.ivSelectCategoryIcon.setOnClickListener {
            setDialog(
                getString(R.string.label_dialog_title_category),
                ItemStorage.getCategory(),
                Constants.DIALOG_TAG_CATEGORY,
                binding.tvPostSelectedCategory,
            ) {
                isCategorySelected = true
                checkInputs()
            }
        }

        binding.ivSelectLanguageIcon.setOnClickListener {
            setDialog(
                getString(R.string.label_dialog_title_language),
                ItemStorage.getLanguage(),
                Constants.DIALOG_TAG_LANGUAGE,
                binding.tvPostSelectedLanguage,
            ) {
                isLanguageSelected = true
                checkInputs()
            }
        }

        binding.ivSelectLocationIcon.setOnClickListener {
            startForResult.launch(Intent(this, LocationActivity::class.java))
        }

        binding.btnPostComplete
    }

    private fun setDialog(
        dialogTitle: String,
        dialogItemList: List<String>,
        dialogTag: String,
        selectedTextView: TextView,
        itemSelected: () -> Unit
    ) {
        val dialog = ItemPickDialogFragment(
            dialogTitle,
            dialogItemList
        ) { selectedItem ->
            selectedTextView.text = selectedItem
            itemSelected()
        }
        dialog.show(supportFragmentManager, dialogTag)
    }

    private fun checkInputs() {
        binding.btnPostComplete.isEnabled =
            binding.etPostTitle.text.toString().isNotBlank()
                    && binding.etPostBody.text.toString().isNotBlank()
                    && binding.etPostNumberOfMember.text.toString().isNotBlank()
                    && isLocationSelected
                    && isMeetingDateSelected
                    && isCategorySelected
                    && isLanguageSelected
    }
}
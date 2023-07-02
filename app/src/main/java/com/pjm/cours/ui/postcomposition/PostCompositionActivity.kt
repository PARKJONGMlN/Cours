package com.pjm.cours.ui.postcomposition

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.datepicker.MaterialDatePicker
import com.pjm.cours.R
import com.pjm.cours.data.ItemStorage
import com.pjm.cours.databinding.ActivityPostCompositionBinding
import com.pjm.cours.ui.location.LocationActivity
import com.pjm.cours.util.Constants
import com.pjm.cours.util.DateFormat

class PostCompositionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostCompositionBinding
    private val viewModel: PostCompositionViewModel by viewModels {
        PostCompositionViewModel.provideFactory()
    }
    private val startForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            val location = result.data?.getStringExtra(Constants.SELECTED_LOCATION) ?: ""
            viewModel.setLocation(location)
            viewModel.setLocationSelection(true)
        } else {
            viewModel.setLocation(getString(R.string.label_post_select_location_message))
            viewModel.setLocationSelection(false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostCompositionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setLayout()
        setObserver()
    }

    private fun setLayout() {
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        binding.appBarPostComposition.setNavigationOnClickListener {
            finish()
        }
        binding.ivSelectDateIcon.setOnClickListener {
            val datePicker =
                MaterialDatePicker.Builder.datePicker()
                    .setTitleText(R.string.label_post_select_date_message)
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build()
            datePicker.addOnPositiveButtonClickListener {
                viewModel.setMeetingDate(DateFormat.convertDisplayDate(it))
                viewModel.setMeetingDateSelection(true)
            }
            datePicker.show(supportFragmentManager, "SELECT_DATE")
        }
        binding.ivSelectCategoryIcon.setOnClickListener {
            setDialog(
                getString(R.string.label_dialog_title_category),
                ItemStorage.getCategory(),
                Constants.DIALOG_TAG_CATEGORY,
            ) {selectedItem ->
                viewModel.setCategory(selectedItem)
                viewModel.setCategorySelection(true)
            }
        }
        binding.ivSelectLanguageIcon.setOnClickListener {
            setDialog(
                getString(R.string.label_dialog_title_language),
                ItemStorage.getLanguage(),
                Constants.DIALOG_TAG_LANGUAGE,
            ) {selectedItem ->
                viewModel.setLanguage(selectedItem)
                viewModel.setLanguageSelection(true)
            }
        }
        binding.ivSelectLocationIcon.setOnClickListener {
            startForResult.launch(Intent(this, LocationActivity::class.java))
        }
        binding.btnPostComplete.setOnClickListener {
            finish()
        }
    }

    private fun setObserver() {
        viewModel.location.observe(this) {
            binding.tvPostSelectedLocation.text = it.peekContent()
        }
        viewModel.meetingDate.observe(this) {
            binding.tvPostSelectedDate.text = it.peekContent()
        }
        viewModel.category.observe(this) {
            binding.tvPostSelectedCategory.text = it.peekContent()
        }
        viewModel.language.observe(this) {
            binding.tvPostSelectedLanguage.text = it.peekContent()
        }
    }

    private fun setDialog(
        dialogTitle: String,
        dialogItemList: List<String>,
        dialogTag: String,
        itemSelected: (selectedItem: String) -> Unit
    ) {
        val dialog = ItemPickDialogFragment(
            dialogTitle,
            dialogItemList
        ) { selectedItem ->
            itemSelected(selectedItem)
        }
        dialog.show(supportFragmentManager, dialogTag)
    }

}
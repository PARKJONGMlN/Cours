package com.pjm.cours.ui.postcomposition

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.pjm.cours.R
import com.pjm.cours.data.ItemStorage
import com.pjm.cours.databinding.ActivityPostCompositionBinding
import com.pjm.cours.ui.chat.ChatActivity
import com.pjm.cours.ui.common.ProgressDialogFragment
import com.pjm.cours.ui.location.LocationActivity
import com.pjm.cours.util.Constants
import com.pjm.cours.util.DateFormat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PostCompositionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostCompositionBinding
    private val viewModel: PostCompositionViewModel by viewModels()
    private val startForResult = getResultLauncher()
    private val dialogLoading = ProgressDialogFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostCompositionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        setLayout()
    }

    private fun setLayout() {
        setAppBar()
        setErrorMessage()
        setLoading()
        setComplete()
        setSelectButton()
    }

    private fun setSelectButton() {
        setDateSelector()
        setCategorySelector()
        setLanguageSelector()
        setLocationSelector()
    }

    private fun setLocationSelector() {
        binding.ivSelectLocationIcon.setOnClickListener {
            startForResult.launch(Intent(this, LocationActivity::class.java))
        }
    }

    private fun setLanguageSelector() {
        binding.ivSelectLanguageIcon.setOnClickListener {
            setDialog(
                getString(R.string.label_dialog_title_language),
                ItemStorage.getLanguage(),
                Constants.DIALOG_TAG_LANGUAGE,
            ) { selectedItem ->
                viewModel.setLanguage(selectedItem)
                viewModel.setLanguageSelection(true)
            }
        }
    }

    private fun setCategorySelector() {
        binding.ivSelectCategoryIcon.setOnClickListener {
            setDialog(
                getString(R.string.label_dialog_title_category),
                ItemStorage.getCategory(),
                Constants.DIALOG_TAG_CATEGORY,
            ) { selectedItem ->
                viewModel.setCategory(selectedItem)
                viewModel.setCategorySelection(true)
            }
        }
    }

    private fun setDateSelector() {
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
    }

    private fun setAppBar() {
        binding.appBarPostComposition.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setComplete() {
        lifecycleScope.launch {
            viewModel.isCompleted.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collect { isCompleted ->
                    if (isCompleted) {
                        Intent(this@PostCompositionActivity, ChatActivity::class.java).apply {
                            putExtra(Constants.POST_ID, viewModel.postId.value)
                            startActivity(this)
                        }
                        finish()
                    }
                }
        }
    }

    private fun setErrorMessage() {
        lifecycleScope.launch {
            viewModel.isError.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collect { isError ->
                    if (isError) {
                        Snackbar.make(
                            binding.root,
                            getString(R.string.error_message),
                            Snackbar.LENGTH_SHORT
                        )
                            .setAnchorView(binding.btnPostComplete)
                            .show()
                    }
                }
        }
    }

    private fun setLoading() {
        lifecycleScope.launch {
            viewModel.isLoading.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collect { isLoading ->
                    if (isLoading) {
                        dialogLoading.show(
                            supportFragmentManager,
                            Constants.DIALOG_FRAGMENT_PROGRESS_TAG
                        )
                    } else {
                        if(dialogLoading.isAdded){
                            dialogLoading.dismiss()
                        }
                    }
                }
        }
    }

    private fun getResultLauncher() = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            val location = result.data?.getStringExtra(Constants.SELECTED_LOCATION) ?: ""
            val locationLatitude =
                result.data?.getStringExtra(Constants.SELECTED_LOCATION_LATITUDE) ?: ""
            val locationLongitude =
                result.data?.getStringExtra(Constants.SELECTED_LOCATION_LONGITUDE) ?: ""
            viewModel.setLocation(location)
            viewModel.setLocationPoint(locationLatitude, locationLongitude)
            viewModel.setLocationSelection(true)
        } else {
            viewModel.setLocation(getString(R.string.label_post_select_location_message))
            viewModel.setLocationSelection(false)
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
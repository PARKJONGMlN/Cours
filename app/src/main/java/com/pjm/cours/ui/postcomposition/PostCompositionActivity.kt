package com.pjm.cours.ui.postcomposition

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.datepicker.MaterialDatePicker
import com.pjm.cours.R
import com.pjm.cours.data.ItemStorage
import com.pjm.cours.databinding.ActivityPostCompositionBinding
import com.pjm.cours.util.Constants
import com.pjm.cours.util.DateFormat

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

        binding.ivSelectDateIcon.setOnClickListener {
            val datePicker =
                MaterialDatePicker.Builder.datePicker()
                    .setTitleText(R.string.label_post_select_date_message)
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build()
            datePicker.addOnPositiveButtonClickListener {
                binding.tvPostSelectedDate.text = DateFormat.convertDisplayDate(it)
            }
            datePicker.show(supportFragmentManager, "SELECT_DATE")
        }

        binding.ivSelectCategoryIcon.setOnClickListener {
            setDialog(
                getString(R.string.label_dialog_title_category),
                ItemStorage.getCategory(),
                Constants.DIALOG_TAG_CATEGORY,
                binding.tvPostSelectedCategory
            )
        }

        binding.ivSelectLanguageIcon.setOnClickListener {
            setDialog(
                getString(R.string.label_dialog_title_language),
                ItemStorage.getLanguage(),
                Constants.DIALOG_TAG_LANGUAGE,
                binding.tvPostSelectedLanguage
            )
        }
    }

    private fun setDialog(
        dialogTitle: String,
        dialogItemList: List<String>,
        dialogTag: String,
        selectedTextView: TextView
    ) {
        val dialog = ItemPickDialogFragment(
            dialogTitle,
            dialogItemList
        ) { selectedItem ->
            selectedTextView.text = selectedItem
        }
        dialog.show(supportFragmentManager, dialogTag)
    }
}
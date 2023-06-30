package com.pjm.cours.ui.postcomposition

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.datepicker.MaterialDatePicker
import com.pjm.cours.R
import com.pjm.cours.databinding.ActivityPostCompositionBinding
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
            datePicker.show(supportFragmentManager,"SELECT_DATE")
        }
    }
}
package com.pjm.cours.ui.common

import android.app.Dialog
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.pjm.cours.databinding.DialogeProgressLoadingBinding

class ProgressDialogFragment : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = DialogeProgressLoadingBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            setCancelable(false)
            setCanceledOnTouchOutside(false)
            setOnKeyListener { _, keyCode, event ->
                return@setOnKeyListener keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP
            }
        }
    }
}
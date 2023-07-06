package com.pjm.cours.ui.common

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.pjm.cours.databinding.ViewProgressBinding

class ProgressView(context: Context, attrs: AttributeSet?): ConstraintLayout(context,attrs) {

    init {
        ViewProgressBinding.inflate(LayoutInflater.from(context),this)
    }
}
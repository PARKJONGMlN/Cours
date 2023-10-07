package com.pjm.cours.ui.common

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.pjm.cours.util.DateFormat.getRelativeTime

@BindingAdapter("relativeTime")
fun bindRelativeTime(textView: TextView, timestamp: String) {
    if (timestamp == "0" || timestamp.isEmpty()) {
        textView.text = ""
    } else {
        textView.text = getRelativeTime(timestamp.toLong())
    }
}

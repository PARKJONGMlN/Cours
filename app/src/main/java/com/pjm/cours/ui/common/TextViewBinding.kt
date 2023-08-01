package com.pjm.cours.ui.common

import android.util.Log
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.pjm.cours.util.DateFormat.getRelativeTime

@BindingAdapter("relativeTime")
fun bindRelativeTime(textView: TextView, timestamp: String) {
    Log.d("TAG", "timestamp: ${timestamp}")
    if(timestamp == "0" || timestamp.isEmpty()){
        textView.text = ""
    } else {
        textView.text = getRelativeTime(timestamp.toLong())
    }
}
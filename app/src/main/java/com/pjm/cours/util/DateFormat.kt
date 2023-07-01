package com.pjm.cours.util

import java.text.SimpleDateFormat
import java.util.*

object DateFormat {

    private const val DATE_YEAR_MONTH_DAY = "yyyy. MM. dd"

    fun convertDisplayDate(long: Long): String {
        val simpleDateFormat = SimpleDateFormat(DATE_YEAR_MONTH_DAY, Locale.KOREA)
        val date = Date()
        date.time = long
        return simpleDateFormat.format(date)
    }
}
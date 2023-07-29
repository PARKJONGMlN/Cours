package com.pjm.cours.util

import java.text.SimpleDateFormat
import java.util.*

object DateFormat {

    private const val DATE_YEAR_MONTH_DAY_PATTERN = "yyyy. MM. dd"
    private const val DATE_YEAR_MONTH_DAY_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'"
    private const val DATE_MONTH_DATE_TIME = "MM월 dd일 HH:mm"

    fun convertDisplayDate(long: Long): String {
        val simpleDateFormat = SimpleDateFormat(DATE_YEAR_MONTH_DAY_PATTERN, Locale.KOREA)
        val date = Date()
        date.time = long
        return simpleDateFormat.format(date)
    }

    fun convertTimestamp(timestamp: String): String{
        return SimpleDateFormat(DATE_MONTH_DATE_TIME, Locale.KOREA).format(timestamp.toLong())
    }

    fun getCurrentTime():String {
        return applyDateFormat(DATE_YEAR_MONTH_DAY_TIME_PATTERN)
    }

    private fun applyDateFormat(pattern: String): String{
        val formatter = SimpleDateFormat(pattern, Locale.KOREA)
        val date = Calendar.getInstance(TimeZone.getTimeZone("UTC")).time
        return formatter.format(date)
    }

    fun getRelativeTime(timestamp: Long): String {
        val formatter = SimpleDateFormat("aa hh:mm", Locale.KOREA)
        return formatter.format(Date(timestamp))
    }

}
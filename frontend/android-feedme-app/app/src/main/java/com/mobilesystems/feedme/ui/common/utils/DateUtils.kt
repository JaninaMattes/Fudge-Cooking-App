package com.mobilesystems.feedme.ui.common.utils

import android.icu.text.DateFormat
import android.icu.text.SimpleDateFormat
import android.net.ParseException
import java.util.*
import java.util.concurrent.TimeUnit

fun getTimeDiff(dateStr: String): Long {
    var result: Long = 0
    if(dateStr.isNotEmpty()) {
        val myFormat = "dd.MM.yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.GERMANY)
        val endDateValue: Date = sdf.parse(dateStr)
        val startDateValue = Calendar.getInstance().timeInMillis
        result = TimeUnit.MILLISECONDS.toDays(endDateValue.time - startDateValue)
    }
    return result
}

fun addDaysToCurrentDate(days: Int): Calendar {
    val currentDate = getCurrentDate()
    currentDate.add(Calendar.DATE, days)
    return currentDate
}

fun getCurrentDate(): Calendar {
    val calendar =  Calendar.getInstance()
    val currentDate = calendar.time
    calendar.time = currentDate
    return calendar
}

fun convertDateFormat(dateStr: String): String {
    // formats a date string into the correct format
    val inputFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd")
    val outputFormat: DateFormat = SimpleDateFormat("dd.MM.yyyy")
    var inputDate = Date()
    try {
        inputDate = inputFormat.parse(dateStr)
    } catch (e: ParseException) {
        e.printStackTrace()
    }
    return outputFormat.format(inputDate) ?: dateStr
}

fun reconvertDateFormat(dateStr: String): String{
    val inputFormat: DateFormat =SimpleDateFormat("dd.MM.yyyy")
    val outputFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd")
    var inputDate = Date()
    try {
        inputDate = inputFormat.parse(dateStr)
    } catch (e: ParseException) {
        e.printStackTrace()
    }
    return outputFormat.format(inputDate) ?: dateStr
}

fun convertStringToDate(dateStr: String): Date {
    val sdf = SimpleDateFormat("dd.MM.yyyy")
    val date = sdf.parse(dateStr)
    return date
}

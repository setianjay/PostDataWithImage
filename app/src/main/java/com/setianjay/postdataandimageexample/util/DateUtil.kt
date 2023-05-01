package com.setianjay.postdataandimageexample.util

import java.text.SimpleDateFormat
import java.util.*

object DateUtil {

    fun dateWithFormat(pattern: String): String{
        val current = Calendar.getInstance().time
        val formatter = SimpleDateFormat(pattern, Locale.getDefault())
        return formatter.format(current)
    }
}
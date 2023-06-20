package com.bimalghara.mp3downloader.utils

import android.util.Log
import java.text.DecimalFormat

fun Long?.formatCountsNumber(): String {
    if(this == null)
        return "0"

    val suffixes = listOf("", "K", "M", "B")
    var value = this.toDouble()
    var suffixIndex = 0

    while (value >= 1000 && suffixIndex < suffixes.size - 1) {
        value /= 1000
        suffixIndex++
    }

    val numberFormat = DecimalFormat("#,##0.#")
    val formattedValue = numberFormat.format(value)
    return "$formattedValue${suffixes[suffixIndex]}"
}

fun getTimeValuesFromString(rawString: String): Triple<Long,Long,Float> {
    var h:Long = 0
    var m:Long = 0
    var s:Float = 0.0F

    val timeMatch = Regex("time=([0-9]+):([0-9]+):([0-9.]+)").find(rawString)
    if(timeMatch!=null) {
        h = timeMatch.groupValues[1].toLong()
        m = timeMatch.groupValues[2].toLong()
        s = timeMatch.groupValues[3].toFloat()
        Log.e("FunUtil", "convert getTimeValuesFromString: h=$h, m=$m,s=$s [$rawString]")
    }

    return Triple(h,m,s)
}
fun getDisplayTimeValue(seconds: Int): String {
    if(seconds < 0)
        return "0"

    val h = (seconds / 3600)
    val m = ((seconds % 3600) / 60)
    val s = ((seconds % 3600) % 60)
    Log.e("FunUtil", "convert getDisplayTimeValue: h=$h, m=$m,s=$s [$seconds]")

    return when {
        h > 0 -> String.format("%d:%02d:%02d", h, m, s)
        m > 0 -> String.format("%d:%02d", m, s)
        else -> String.format("%d", s)
    }
}

package com.example.pomodoro

import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.round

class ProgressConverter(sharedPreference1: SharedPreferences) {
    val formatter = SimpleDateFormat("dd/MM/yyyy ")
    val currentDate = formatter.format(Date())
    private val duration = sharedPreference1.getInt(currentDate, 0) // if there's no record, return 0 as default

    fun progressCal(defaultAim: Int):Float{
        val remainder = duration/25f*5f  // the unused 5 mins per session of 25 mins will be counted
        val todayCount = (duration + remainder)/60
        return round(todayCount/defaultAim*100)
    }

    fun duration(): Int{
        return duration
    }
}
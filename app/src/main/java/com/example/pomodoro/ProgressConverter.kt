package com.example.pomodoro

import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.round

class ProgressConverter(runTime: Int) {
    val formatter = SimpleDateFormat("dd/MM/yyyy")
    val currentDate = formatter.format(Date())
    private val runTime = runTime


    fun progressCal(defaultAim: Int):Float{

        val remainder = runTime/25f*5f  // the unused 5 mins per session of 25 mins will be counted
        val todayCount = (runTime + remainder)/60
        return round(todayCount/defaultAim*100)
    }

    fun runTime(): Int{
        return runTime
    }
}
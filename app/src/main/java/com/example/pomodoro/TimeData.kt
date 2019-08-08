package com.example.pomodoro

class TimeData(setTime: Long) {
    val duration: Long = setTime // extracted later again
    val minute: Long = 1000 * 60 // 1000 milliseconds = 1 second
    val second: Long = 1000
    val accelerator: Long = 250 // set to 1 in normal mode, used for testing/debugging
    val durtaionInMin: Long = minute * setTime / accelerator

    val countDownInterval: Long = 1000 // normal case: Count Down interval 1 second

}
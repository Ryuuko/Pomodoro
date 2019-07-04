package com.example.pomodoro

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import kotlinx.android.synthetic.main.activity_working.*
import kotlin.concurrent.timer
import com.example.pomodoro.TimeData

class working : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_working)
        val timeData: TimeData = TimeData(1)

        // count down
        val timer = object : CountDownTimer(timeData.timeLeft, timeData.countDownInterval) {
            override fun onTick(millisUntilFinished: Long) {

                val minuteLeft = millisUntilFinished/timeData.minute
                val secondLeft = millisUntilFinished%timeData.minute/timeData.second
                timeReminder.setText("Time Left " + minuteLeft + ":" + secondLeft)

                val timePercent = ((timeData.timeLeft-millisUntilFinished)*100/timeData.timeLeft).toInt()
                progressBar.setProgress(timePercent)
            }

            override fun onFinish() {
                fullfillcircle.visibility = View.VISIBLE
                //  animation with fadein

                YoYo.with(Techniques.FadeIn)
                    .duration(5000)
                    .repeat(0)
                    .playOn(fullfillcircle);

                timeReminder.setText("Time's finished!")

            }
        }
        timer.start()
    }
}

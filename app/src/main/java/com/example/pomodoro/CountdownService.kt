package com.example.pomodoro

import android.app.*
import android.content.Intent
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log


class CountdownService : Service() {

    private lateinit var timeData: TimeData
    private lateinit var timer: CountDownTimer

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if(intent!=null){

            val duration = intent?.getStringExtra("duration").toLong()
            timeData = TimeData(duration)
            countdown()
        }

        return START_NOT_STICKY // using START_STICKY will restart the service even the it's once stopped
    }

    fun countdown(){

        val timerInfo = Intent()
        timer = object : CountDownTimer(timeData.durationMillis+1000, timeData.countDownInterval) {
            // add 1000 Milliseconds or 1 second to compensate the delay, like 1 min will display 0:58 at first without this compensation
            //todo optimize the code to improve the speed between activity switching without using this trick....
            override fun onTick(millisUntilFinished: Long) {

                timerInfo.action = "timeLeft"
                timerInfo.putExtra("millisUntilFinished", millisUntilFinished)
                sendBroadcast(timerInfo)

            }

            override fun onFinish() {

                timerInfo.action = "finished"
                sendBroadcast(timerInfo)
            }
        }
        timer.start()
    }

    
    override fun onBind(intent: Intent?): IBinder? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onDestroy() {
        Log.d("destroyme", "kick me ass plz")
        timer.cancel()
        super.onDestroy()
    }

}

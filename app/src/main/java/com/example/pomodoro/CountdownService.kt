package com.example.pomodoro

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlin.concurrent.thread
import android.widget.Toast
import java.util.*



class CountdownService : Service() {

    private lateinit var timeData: TimeData
    private lateinit var timer: CountDownTimer
    private lateinit var builder: Notification.Builder
    private lateinit var manager: NotificationManager

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if(intent!=null){

            val duration = intent.getStringExtra("duration").toLong()
            timeData = TimeData(duration)
            makeNotification()
            countdown()
        }

        return START_NOT_STICKY // using START_STICKY will restart the service even the it's once stopped
    }

    fun countdown(){

        val timerInfo = Intent()
        timer = object : CountDownTimer(timeData.durationMillis, timeData.countDownInterval) {

            override fun onTick(millisUntilFinished: Long) {

                timerInfo.action = "timeLeft"
                timerInfo.putExtra("millisUntilFinished", millisUntilFinished)
                sendBroadcast(timerInfo)
                // calculate the time left each interval i.e. each second
                val minuteLeft = millisUntilFinished/timeData.minute
                val secondLeft = millisUntilFinished%timeData.minute/timeData.second

                // display the time
                val timeMessage = "Time Left $minuteLeft : $secondLeft"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    // calculate the time left each interval i.e. each second

                    builder.setContentText(timeMessage)
                    val notification = builder.build()
                    manager.notify(CountdownService.ChannelID, notification)
                }

            }

            override fun onFinish() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    builder.setContentTitle("Session Finished")
                          .setContentText("END GAMEヾ(⌐■_■)ノ♪")
                    val notification = builder.build()
                    manager.notify(CountdownService.ChannelID, notification)
                    timerInfo.action = "finished"
                    sendBroadcast(timerInfo)
                }
            }
        }
        timer.start()
    }

    fun makeNotification(){
        // Create channel for new Android versions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CountdownService.ChannelID.toString(), CountdownService.ChannelName,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.setSound(null, null) // disable the sound from IMPORTANCE HIGH notification
            channel.enableVibration(false)

            manager = getSystemService(AppCompatActivity.NOTIFICATION_SERVICE)
                    as NotificationManager
            manager.createNotificationChannel(channel)

            builder = Notification.Builder(this, CountdownService.ChannelID.toString())
                .setContentTitle("Remember your faith")
                .setContentText("ouioui!")
                .setOnlyAlertOnce(true)
                .setSmallIcon(R.drawable.suttomarino)

            // when user clicks the notification, launch the Working activity
            val intent = Intent(this, Working::class.java)
            val pendingIntent = PendingIntent.getActivity(
                this, 0, intent, 0
            )
            builder.setContentIntent(pendingIntent)

            val notification = builder.build()
            manager.notify(CountdownService.ChannelID, notification)
            startForeground(ChannelID, notification)
        }
    }

    companion object{
        // Id code that is used to launch the time notifications
        private const val ChannelName = "trackTimeNotify"
        private const val ChannelID = 999
    }


    override fun onBind(intent: Intent?): IBinder? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onDestroy() {
        timer.cancel()
        super.onDestroy()
    }

}

package com.example.pomodoro

import android.app.*
import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.SystemClock
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.VISIBLE
import android.widget.ImageView
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import kotlinx.android.synthetic.main.activity_working.*
import java.text.SimpleDateFormat
import java.util.*


class working : AppCompatActivity() {

    lateinit var timeData: TimeData
    lateinit var mp: MediaPlayer
    lateinit var builder: Notification.Builder
    private var private_mode = 0
    private val pref_name = "RunningTime"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_working)

        val duration = intent.getStringExtra("duration").toLong()
        timeData = TimeData(duration, 200)

        // start the music
        musicSetup()

        // set up notification
        makeNotification()

        // count down
        val timer = object : CountDownTimer(timeData.timeLeft, timeData.countDownInterval) {
            override fun onTick(millisUntilFinished: Long) {
                // calculate the time left each interval i.e. each second
                val minuteLeft = millisUntilFinished/timeData.minute
                val secondLeft = millisUntilFinished%timeData.minute/timeData.second

                val timeMessage = "Time Left " + minuteLeft + ":" + secondLeft
                timeReminder.setText(timeMessage)

                // the progress bar will reflect the time left
                val timePercent = ((timeData.timeLeft-millisUntilFinished)*100/timeData.timeLeft).toInt()
                progressBar.setProgress(timePercent)

            }

            override fun onFinish() {
                fullfillcircle.visibility = View.VISIBLE
                //  animation with fadein
                fadeIn(fullfillcircle)
                // todo: timeReminder.setText("Finished! Click me if you want to overrun")
                goHome.setText("New Session?")

                fullfillcircle.setOnTouchListener { _, event ->
                    if(event.action == MotionEvent.ACTION_DOWN){
                        circleChange()
                        chronometerStart()
                        fullfillcircle.isEnabled = false
                    }
                    true
                }
            }
        }
        timer.start()
    }

    fun fadeIn(circle: ImageView){
        YoYo.with(Techniques.FadeIn)
            .duration(1000)
            .repeat(0)
            .playOn(circle);
    }

    fun circleChange(){
        elapsecircle.visibility = VISIBLE
        fadeIn(elapsecircle)
    }

    fun chronometerStart(){
        chronometer.base = SystemClock.elapsedRealtime()
        chronometer.start()
        chronometer.format = "Time Elapsed: %s"
        chronometer.visibility = VISIBLE
    }

    fun goHome(view: View){
        var total = 0
        if(fullfillcircle.visibility == VISIBLE){
            total = timeData.duration.toInt()
        }
        if(chronometer.visibility == VISIBLE){
            val elapsedMillis = (SystemClock.elapsedRealtime() - chronometer.base)/timeData.minute
            total = (elapsedMillis + timeData.duration).toInt()
        }
        recordSaver(total)
        val myIntent = Intent(this, MainActivity::class.java)
        startActivity(myIntent) // jump back to the start page

    }

    fun recordSaver(recordUpdate: Int){
        val formatter = SimpleDateFormat("dd/MM/yyyy ")
        val currentDate = formatter.format(Date())
        val sharedPreference: SharedPreferences = this.getSharedPreferences(pref_name, private_mode)
        val oldRecord = sharedPreference.getFloat(currentDate, 0f).toInt() // if there's no record, return 0 as default
        Log.d("time", oldRecord.toString())
        Log.d("time", recordUpdate.toString())
        val newRecord = oldRecord + recordUpdate
        Log.d("time", newRecord.toString())
        val editor: SharedPreferences.Editor = sharedPreference.edit()
        editor.putFloat(currentDate, newRecord.toFloat())
        editor.commit()
    }

    fun musicSetup(){
        var first: Boolean = true
        musicbutton.setOnClickListener {
            if(first){
                mp = MediaPlayer.create(this, R.raw.rainyshort)
                Log.d("music", "playing first time")
                musicbutton.setImageResource(R.drawable.radiodark)
                mp.start()
                mp.isLooping = true
                first = false
            }
            else if(mp.isPlaying){
                Log.d("music", "stop")
                musicbutton.setImageResource(R.drawable.radiolight)
                mp.stop()
            }
            else if(!mp.isPlaying && !first){
                Log.d("music", "playing again")
                musicbutton.setImageResource(R.drawable.radiodark)
                mp.prepare()
                mp.start()
            }
        }
    }

    fun makeNotification(){
        // Create channel for new Android versions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                ChannelID.toString(), ChannelName,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            Log.d("channel!!!", channel.toString())
            val manager = getSystemService(NOTIFICATION_SERVICE)
                    as NotificationManager
            manager.createNotificationChannel(channel)

            builder = Notification.Builder(this, ChannelID.toString())
                .setContentTitle("Remember your faith")
                .setContentText("ouioui!")
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.suttomarino)

            val notification = builder.build()
            manager.notify(ChannelID, notification)
        }
    }

    companion object{
        // Id code that is used to launch the time notifications
        private const val ChannelName = "trackTimeNotify"
        private const val ChannelID = 999
    }

}

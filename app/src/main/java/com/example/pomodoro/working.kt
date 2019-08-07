package com.example.pomodoro

import android.app.*
import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
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

    private lateinit var timeData: TimeData
    private lateinit var mp: MediaPlayer
    private lateinit var builder: Notification.Builder
    private var private_mode = 0
    private val pref_name = "RunningTime"
    private val pref_name2 = "DefaultSetting"
    private lateinit var userPref1: UserPref
    private lateinit var userPref2: UserPref
    private lateinit var thisactivity: Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_working)

        thisactivity = this
        val duration = intent.getStringExtra("duration").toLong()
        timeData = TimeData(duration, 250)

        /* create variables for setting up*/
        val sharedPreference2: SharedPreferences = this.getSharedPreferences(pref_name2, private_mode)
        val editor2: SharedPreferences.Editor = sharedPreference2.edit()

        // separate two classes in order not to mess up with the savedsound variable, which needs to be treated differently in onFinish() function
        userPref1 = UserPref(sharedPreference2, editor2)
        userPref2 = UserPref(sharedPreference2, editor2)

        userPref1.soundSetup(this, "endsound", endsound) // set up the dialog and icon of end sound
        userPref2.soundSetup(this, "musicsound", musicsound) // set up the dialog and icon of radio box

        // count down
        countdown()
    }

    fun countdown(){
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
                userPref2.releaseLast() // stop the music
                fullfillcircle.visibility = View.VISIBLE
                //  animation with fadein
                fadeIn(fullfillcircle)
                // todo: timeReminder.setText("Finished! Click me if you want to overrun")

                if(userPref1.savedsound()!="null") {
                    val selected = userPref1.savedsound().toLowerCase()
                    Log.d("hey", selected)
                    val ID = thisactivity.resources.getIdentifier(selected,
                        "raw", "com.example.pomodoro")
                    val mpEndSound = MediaPlayer.create(thisactivity, ID)
                    mpEndSound.start()
                    mpEndSound.setOnCompletionListener { mpEndSound.release() } // release the object since start sound will be not used anymore
                }

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
        userPref2.releaseLast() // in case the user has activated the music button (musicsound), stop it here
        startActivity(myIntent) // jump back to the start page

    }

    fun recordSaver(recordUpdate: Int){
        val formatter = SimpleDateFormat("dd/MM/yyyy ")
        val currentDate = formatter.format(Date())
        val sharedPreference: SharedPreferences = this.getSharedPreferences(pref_name, private_mode)
        val oldRecord = sharedPreference.getInt(currentDate, 0) // if there's no record, return 0 as default
        val newRecord = oldRecord + recordUpdate
        Log.d("time", newRecord.toString())
        val editor: SharedPreferences.Editor = sharedPreference.edit()
        editor.putInt(currentDate, newRecord)
        editor.commit()
    }

    companion object{
        // Id code that is used to launch the time notifications
        private const val ChannelName = "trackTimeNotify"
        private const val ChannelID = 999
    }

}

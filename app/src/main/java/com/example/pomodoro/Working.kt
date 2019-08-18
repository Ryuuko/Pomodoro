package com.example.pomodoro

import android.app.*
import android.content.*
import android.media.MediaPlayer
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.Toast
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import kotlinx.android.synthetic.main.activity_working.*
import java.sql.Time


class Working : AppCompatActivity() {

    private lateinit var timeData: TimeData
    private lateinit var mp: MediaPlayer
    private lateinit var builder: Notification.Builder
    private var private_mode = 0
    private val pref_name = "RunningTime"
    private val pref_name2 = "DefaultSetting"

    private lateinit var userPref1: UserPref
    private lateinit var userPref2: UserPref
    private lateinit var thisactivity: Activity
    private lateinit var timeReceiver: BroadcastReceiver
    private lateinit var manager: NotificationManager

    private var notiOn = false
    private var destroy = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // enable full screen
        try { this.supportActionBar!!.hide()
        } catch (e: NullPointerException) { }

        setContentView(R.layout.activity_working)

        Log.d("heyyy", "I've been created")

        thisactivity = this
        val duration = intent.getStringExtra("duration")
        timeData = TimeData(duration.toLong())

        val intent1 = Intent(this, CountdownService::class.java)
        intent1.putExtra("duration", duration) // the first array will be the duration number
        startService(intent1)

        /* create variables for setting up*/
        val sharedPreference: SharedPreferences = this.getSharedPreferences(pref_name2, private_mode)
        val editor: SharedPreferences.Editor = sharedPreference.edit()

        // separate two classes in order not to mess up with the savedSound variable, which needs to be treated differently in onFinish() function
        userPref1 = UserPref(sharedPreference, editor)
        userPref2 = UserPref(sharedPreference, editor)

        userPref1.soundSetup(this, "endsound", endsound) // set up the dialog and icon of end sound
        Log.d("heyyy", "the end sound is${userPref1.savedsound()}")
        userPref2.soundSetup(this, "musicsound", musicsound) // set up the dialog and icon of radio box
        Log.d("heyyy", "the music sound is${userPref2.savedsound()}")



            setBroadcast()
            Log.d("heyyy", "register first")
            // listen for broadcast every seconds as the service's runing
            val filter = IntentFilter()
            filter.addAction("timeLeft")
            filter.addAction("finished")
            this.registerReceiver(timeReceiver, filter)


    }

    fun setBroadcast(){
        timeReceiver = object : BroadcastReceiver(){
            override fun onReceive(context: Context?, intent: Intent?) {
                if(intent!=null){

                    if(intent.action=="timeLeft"){

                        // calculate the time left each interval i.e. each second
                        val millisUntilFinished = intent.getLongExtra("millisUntilFinished", 0)
                        val minuteLeft = millisUntilFinished/timeData.minute
                        val secondLeft = millisUntilFinished%timeData.minute/timeData.second

                        // display the time
                        val timeMessage = "Time Left $minuteLeft : $secondLeft"
                        timeReminder.setText(timeMessage)
                        Log.d("heyyy", "I've recieved$timeMessage")
                        // the progress bar will reflect the time left
                        Log.d("durationMillis", timeData.durationMillis.toString()) // todo why there's two duration value???
                        val timePercent = ((timeData.durationMillis-millisUntilFinished)*100/timeData.durationMillis).toInt()
                        progressBar.setProgress(timePercent)
                    }
                    if(intent.action=="finished"){
                        Log.d("heyyy", "I've recieved the finished request")
                        finishClock()
                    }
                }
            }
        }
    }


    override fun onBackPressed() {
        // do actually nothing, disable back button
        Toast.makeText(this, "I think you should click the button above instead (｡ŏ_ŏ)",
            Toast.LENGTH_SHORT).show()
    }

    fun finishClock(){
        this.unregisterReceiver(timeReceiver) // unregister the timeReceiver since the session is over
        userPref2.releaseLast() // stop the music
        fullfillcircle.visibility = View.VISIBLE
        //  animation with fadein
        fadeIn(fullfillcircle)

        if(userPref1.savedsound()!="null") {

            //  start with different a different service
            // since restart with ruin the current notification, which isn't intended
            // move the following code to the new service
            val selected = userPref1.savedsound().toLowerCase() // todo userPref1.savedsound() keep creating new instances
            Log.d("heyyy", "I got$selected")
            val intent = Intent(this, EndSoundService::class.java)
            intent.putExtra("selected", selected)
            startService(intent)
        }

        goHome.setText("New Session?")
        clickReminder.visibility = VISIBLE
        YoYo.with(Techniques.FadeIn)
            .duration(500)
            .repeat(0)
            .playOn(clickReminder);

        fullfillcircle.setOnClickListener {
            clickReminder.visibility = GONE
            Toast.makeText(this, "Your extra effort will be not forgotten (ง๑ •̀_•́)ง",
                Toast.LENGTH_SHORT).show()
            circleChange()
            chronometerStart()
            fullfillcircle.isEnabled = false // it can be clickable only once
        }
    }

    fun fadeIn(imageView: ImageView){
        YoYo.with(Techniques.FadeIn)
            .duration(500)
            .repeat(0)
            .playOn(imageView);
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

            // record the elapsed time if it has been ever run
            if(chronometer.visibility == VISIBLE){
                val elapsedMillis = (SystemClock.elapsedRealtime() - chronometer.base)/timeData.minute
                total = (elapsedMillis + timeData.duration).toInt()
            }

            recordSaver(total)

        }
        else{Toast.makeText(this, "It will not be counted if you give it up!!! Σ(;ﾟдﾟ)",
            Toast.LENGTH_SHORT).show()}

        val myIntent = Intent(this, MainActivity::class.java)
        userPref2.releaseLast() // in case the user has activated the music button (musicsound), stop it here
        startActivity(myIntent) // jump back to the start page

        finish() // destroy this activity
    }

    fun recordSaver(recordUpdate: Int){
        val sharedPreference2: SharedPreferences = this.getSharedPreferences(pref_name2, private_mode)
        val editor2: SharedPreferences.Editor = sharedPreference2.edit()

        val oldRecord = sharedPreference2.getInt("runTime", 0) // if there's no record, return 0 as default
        Log.d("hey!", oldRecord.toString())
        val newRecord = oldRecord + recordUpdate
        Log.d("hey!", newRecord.toString())
        editor2.putInt("runTime", newRecord)
        editor2.commit()
        Log.d("hey!", sharedPreference2.getInt("runTime", 0).toString())

    }

    override fun onStop() {
        super.onStop()
        Log.d("heyyy", "I'm stopped")
    }

    override fun onRestart() {
        super.onRestart()
        Log.d("heyyy", "I'm restarted")
    }

    override fun onDestroy() {
        //  make sure to clean out the service/timer if the activity is destroyed
        Log.d("heyyy", "I'm destroyed")
        serviceStop()
        super.onDestroy()

    }

    fun serviceStop(){
        // stop the service's countdown timer
        val intent = Intent(this, CountdownService::class.java)
        this.stopService(intent)
    }
}

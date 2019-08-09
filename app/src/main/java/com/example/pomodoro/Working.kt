package com.example.pomodoro

import android.app.*
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.NotificationManager.IMPORTANCE_MAX
import android.content.*
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
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_working.*
import java.text.SimpleDateFormat
import java.util.*


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
    private lateinit var manager: NotificationManager

    private var notiOn = false
    private var destroy = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_working)

        thisactivity = this
        val duration = intent.getStringExtra("duration").toLong()
        timeData = TimeData(duration)

        /* create variables for setting up*/
        val sharedPreference2: SharedPreferences = this.getSharedPreferences(pref_name2, private_mode)
        val editor2: SharedPreferences.Editor = sharedPreference2.edit()

        // separate two classes in order not to mess up with the savedsound variable, which needs to be treated differently in onFinish() function
        userPref1 = UserPref(sharedPreference2, editor2)
        userPref2 = UserPref(sharedPreference2, editor2)

        userPref1.soundSetup(this, "endsound", endsound) // set up the dialog and icon of end sound
        userPref2.soundSetup(this, "musicsound", musicsound) // set up the dialog and icon of radio box

        // listen for broadcast every seconds as the service's runing
        val filter = IntentFilter()
        filter.addAction("timeLeft")
        filter.addAction("finished")
        registerReceiver(timerReceiver(), filter)

        // set up the notification that will be activated only if onStop()
        makeNotification()

    }

    private inner class timerReceiver: BroadcastReceiver(){
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

                    // keep updating notification every second if it's activiated during onStop() phrase,
                    // i.e. the app run in the background
                    if(notiOn){
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                            // calculate the time left each interval i.e. each second

                            builder.setContentText(timeMessage)
                            val notification = builder.build()
                            manager.notify(ChannelID, notification)
                        }
                    }

                    // the progress bar will reflect the time left
                    val timePercent = ((timeData.durtaionInMin-millisUntilFinished)*100/timeData.durtaionInMin).toInt()
                    progressBar.setProgress(timePercent)
                }
                if(intent.action=="finished"){
                    Log.d("finished", "I've recieved the finished request")
                    finishClock()
                }
            }
        }
    }

    fun makeNotification(){
        // Create channel for new Android versions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                ChannelID.toString(), ChannelName,
                NotificationManager.IMPORTANCE_HIGH // head-up notification
            )

//            channel.setSound(null, null) // disable the sound from IMPORTANCE HIGH notification
//            channel.enableVibration(false)

            channel.importance = IMPORTANCE_HIGH // in order to activate a head-up notification

            manager = getSystemService(NOTIFICATION_SERVICE)
                    as NotificationManager
            manager.createNotificationChannel(channel)

            builder = Notification.Builder(this, ChannelID.toString())
                .setContentTitle("Remember your faith")
                .setSmallIcon(R.drawable.suttomarino)


            // when user clicks the notification, resume the Working activity
            val intent = Intent(this, Working::class.java)
            // the intent will avoid creating the activity since its already instantiated
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)

            val pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_NO_CREATE
            )
            builder.setContentIntent(pendingIntent)
        }
    }

    companion object{
        // Id code that is used to launch the time notifications
        private const val ChannelName = "trackTimeNotify"
        private const val ChannelID = 999
    }


    override fun onStop() {
        super.onStop()
        if (!destroy){notiOn=true} // we make notification only when the app runs in the backstage

    }

    override fun onRestart() {
        super.onRestart()
        manager.cancel(ChannelID)
        notiOn = false
    }

    fun finishClock(){
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

        // stop the service's countdown timer as long as clicked
        val intent = Intent(this, CountdownService::class.java)
        this.stopService(intent)

        recordSaver(total)
        val myIntent = Intent(this, MainActivity::class.java)
        userPref2.releaseLast() // in case the user has activated the music button (musicsound), stop it here
        this.destroy = true // since we're going to destroy the activity and notify the onStop() not to activiate makeNotification()
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
}

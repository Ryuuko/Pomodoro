package com.example.pomodoro

import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.triggertrap.seekarc.SeekArc
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread
import kotlin.math.round


class MainActivity : AppCompatActivity() {
    private val REQ_CODE = 123
    private var private_mode = 0
    private val pref_name = "DefaultSetting"
    val formatter = SimpleDateFormat("dd/MM/yyyy")
    private lateinit var userPref: UserPref
    private lateinit var progressConverter: ProgressConverter

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)


        // enable full screen
        try { this.supportActionBar!!.hide()
        } catch (e: NullPointerException) { }

        setContentView(R.layout.activity_main)

        /* create variables for setting up*/
        val sharedPreference: SharedPreferences = this.getSharedPreferences(pref_name, private_mode)
        val editor: SharedPreferences.Editor = sharedPreference.edit()

        val recordDate =  sharedPreference.getString("recordDate", "null") // if there's no record, return null as default

        if(recordDate!="null"){
            val today = formatter.format(Date())
            if(!today.equals(recordDate)){
                // if today's date is after the recorded, aka the record from the past, the record will be refreshed
                Log.d("hey!", "Don't clean me if the record is today or tmr!")
                refresh(editor)
            }
        }else{
            refresh(editor)
        }

        val runTime = sharedPreference.getInt("runTime", 0)

        userPref = UserPref(sharedPreference, editor)
        progressConverter = ProgressConverter(runTime)

        /* default page set up */
        durationreview.setText("You've worked for ${progressConverter.runTime()} mins today " )
        if(userPref.calendartrigger()){
            val progress = progressConverter.progressCal(userPref.defaultaim())
            userPref.progressSetup(progress, this) // calculate and display the progress
        }
        else{
            userPref.progressDisable(this)
        }
        val duration = timeTrans(userPref.session()) // default duration on display
        timeDisplay.text = "$duration mins"
        Log.d("hey!", "the default is $duration")

        /* seekbar set up */
        seekbarControl()

        /* calendar setup */
        userPref.calendarSetup(this)

        /* start sound setup */
        userPref.soundSetup(this, "startsound", startsound)
    }

    fun timeTrans(progress: Int): String{

        val time = ((progress / 4.16).toInt() * 5).toString() // Interval = 5 mins
        return time
    }

    fun seekbarControl(){
        seekBar.progress = userPref.session() // default duration on seekarc
        seekBar.setOnSeekArcChangeListener(object : SeekArc.OnSeekArcChangeListener {
            override fun onStopTrackingTouch(seekArc: SeekArc) {
                // TODO Auto-generated method stub
        }
            override fun onStartTrackingTouch(seekArc: SeekArc) {
                // TODO Auto-generated method stub
            }
            override fun onProgressChanged(
                seekArc: SeekArc, progress: Int,
                fromUser: Boolean
            ) {
                Log.d("hey!", "progress is $progress")
                val time = timeTrans(progress)
                timeDisplay.text = "$time mins"
                Log.d("hey!", "display is $time")
            }
        })
    }

    fun buttonClick(view: View){
        userPref.sessionsave(seekBar.progress)
        val duration = timeDisplay.text.split(" ")[0]

        if(duration == "0"){
            Toast.makeText(this, "Hey! Stop playing me like that!!!  ⁄(⁄ ⁄•⁄ω⁄•⁄ ⁄)⁄", Toast.LENGTH_SHORT).show()
        } // it will crash if 0 min is selected, since the duration number will be used to divide in order to have calculate progress
        else{ startSession(duration) }
    }

    fun startSession(duration: String){

        val intent2 = Intent(this, Working::class.java)
        intent2.putExtra("duration", duration) // the first array will be the duration number

        if(userPref.savedsound()!="null") {
            val selected = userPref.savedsound().toLowerCase()
            val ID = this.resources.getIdentifier(selected,
                "raw", "com.example.pomodoro")
            val mpStartSound = MediaPlayer.create(this, ID)
            mpStartSound.start()
            mpStartSound.setOnCompletionListener {
                mpStartSound.release()
                 } // release the object since start sound will be not used anymore
        }
            Toast.makeText(this, "Here we go~~~Andiamo! ✧*｡٩(ˊᗜˋ*)و✧*｡", Toast.LENGTH_SHORT).show()
            startActivityForResult(intent2, REQ_CODE)
    }

    fun refresh(editor:SharedPreferences.Editor){
        editor.putString("recordDate", formatter.format(Date()))
        editor.putInt("runTime", 0)
        editor.commit()
    }

    override fun onBackPressed() {
        // do actually nothing, disable back button
        Toast.makeText(this, "Plz restart the app if you want to read a random quote again ξ( ✿＞◡❛)",
            Toast.LENGTH_SHORT).show()
    }
}

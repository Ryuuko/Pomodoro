package com.example.pomodoro

import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.triggertrap.seekarc.SeekArc
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private val REQ_CODE = 123
    private var private_mode = 0
    private val pref_name1 = "RunningTime"
    private val pref_name2 = "DefaultSetting"
    private lateinit var userPref: UserPref
    private lateinit var progressConverter: ProgressConverter

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /* create variables for setting up*/
        val sharedPreference1: SharedPreferences = this.getSharedPreferences(pref_name1, private_mode)
        val sharedPreference2: SharedPreferences = this.getSharedPreferences(pref_name2, private_mode)
        val editor2: SharedPreferences.Editor = sharedPreference2.edit()

        userPref = UserPref(sharedPreference2, editor2)
        progressConverter = ProgressConverter(sharedPreference1)

        /* default page set up */
        val progress = progressConverter.progressCal(userPref.defaultaim())
        userPref.progressSetup(progressConverter.total(), progress, this) // calculate and display the progress

        timeTrans(userPref.session()) // default duration on display
        seekbarControl()

        /* calendar setup */
        planbutton.setOnClickListener {
            userPref.dialogSetup(this)
        }

        /* start sound setup */
        userPref.startsoundSetup(this)
    }

    fun timeTrans(progress: Int){
        val time = (progress * 1.2).toInt().toString()
        timeDisplay.text = "$time mins"
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
                timeTrans(progress)
            }
        })
    }

    fun buttonClick(view: View){
        userPref.sessionsave(seekBar.progress)
        val intent = Intent(this, working::class.java)
        intent.putExtra("duration", timeDisplay.text.split(" ")[0]) // the first array will be the duration number
        if(userPref.startsound())
        { MediaPlayer.create(this, R.raw.default0).start() }
        startActivityForResult(intent, REQ_CODE)
    }
}

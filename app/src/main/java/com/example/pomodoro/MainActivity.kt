package com.example.pomodoro

import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.NumberPicker
import com.triggertrap.seekarc.SeekArc
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.round


class MainActivity : AppCompatActivity() {
    private val REQ_CODE = 123
    private var private_mode = 0
    private val pref_name = "RunningTime"
    private val pref_name2 = "DefaultSetting"
    private var total = 0

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /* create variables for setting up*/
        val sharedPreference2: SharedPreferences = this.getSharedPreferences(pref_name2, private_mode)
        var defaultAim = sharedPreference2.getFloat("defaultAim", 2f).toInt() // if there's no record, return 2 as default
        var sessionTime = sharedPreference2.getFloat("sessionTime", 22f).toInt() // default: 25mins

        /*set up*/
        val progress = progressCal(defaultAim)
        progressSetup(progress)
        seekBar.progress =  sessionTime
        timeTrans(sessionTime)
        seekbarControl()

        /*calendar set up*/
        calendarSetup()
    }

    fun progressCal(defaultAim: Int):Float{
        val formatter = SimpleDateFormat("dd/MM/yyyy ")
        val currentDate = formatter.format(Date())

        val sharedPreference: SharedPreferences = this.getSharedPreferences(pref_name, private_mode)
        total = sharedPreference.getFloat(currentDate, 0f).toInt() // if there's no record, return 0 as default

        val remainder = total/25f*5f  // the unused 5 mins per session of 25 mins will be counted
        val todayCount = (total + remainder)/60

        return round(todayCount/defaultAim*100)
    }

    fun progressSetup(progress: Float){
        when{
            progress<100f -> review.setText("You've learnt for $total mins today \n ${progress.toInt()}% of progress has been completed")
            progress==100f -> review.setText("You've learnt for $total mins today \n Congratulations! You've completed today's requirement! Take a rest!")
            progress>100f -> review.setText("You've learnt for $total mins today \n Wow! ${progress.toInt()}% has been made!")
        }
    }

    fun timeTrans(progress: Int){
        val time = (progress * 1.2).toInt().toString()
        timeDisplay.text = "$time mins"
    }

    fun seekbarControl(){
        seekBar.setOnSeekArcChangeListener(object : SeekArc.OnSeekArcChangeListener {
            override fun onStopTrackingTouch(seekArc: SeekArc) {
        }
            override fun onStartTrackingTouch(seekArc: SeekArc) {
            }
            override fun onProgressChanged(
                seekArc: SeekArc, progress: Int,
                fromUser: Boolean
            ) {
                timeTrans(progress)
            }
        })
    }

    fun calendarSetup(){
        planbutton.setOnTouchListener { _, event ->
            if(event.action == MotionEvent.ACTION_DOWN){
                // initialize a new instance
                dialogSetup()
            }
            true
        }
    }

    fun dialogSetup(){

        val npView = layoutInflater.inflate(R.layout.dialog, null)
        val numberPicker = npView.findViewById<NumberPicker>(R.id.numberPicker)

        val sharedPreference2: SharedPreferences = this.getSharedPreferences(pref_name2, private_mode)
        var defaultAim = sharedPreference2.getFloat("defaultAim", 2f).toInt() // if there's no record, return 2 as default

        // set up the picker for aim duration
        numberPicker.maxValue = 6
        numberPicker.minValue = 1
        numberPicker.value = defaultAim
        Log.d("hey!", defaultAim.toString())

        val builder = android.app.AlertDialog.Builder(this)
        // set title of the alert
        builder.setTitle("Set Your Goal")
        builder.setMessage("How many hours do you want to work per day?")
        builder.setView(npView)
            // Add action buttons
            .setPositiveButton("Set",
                DialogInterface.OnClickListener { dialog, _->
                    val selectedValue = numberPicker.value
                    aimReload(numberPicker)
                    dialog.cancel()
                })
            .setNegativeButton("Cancel", null)
        val dialog = builder.create()
        dialog.show()
    }

    fun save(key: String, savedValue: Int){
        val sharedPreference2: SharedPreferences = this.getSharedPreferences(pref_name2, private_mode)
        val editor: SharedPreferences.Editor = sharedPreference2.edit()
        editor.putFloat(key, savedValue.toFloat())
        editor.commit()
    }

    fun aimReload(numberPicker: NumberPicker){
        val defaultAim = numberPicker.value
        save("defaultAim", defaultAim)
        val progress = progressCal(defaultAim)
        progressSetup(progress)
    }

    fun buttonClick(view: View){
        save("sessionTime", seekBar.progress)
        val intent = Intent(this, working::class.java)
        intent.putExtra("duration", timeDisplay.text.split(" ")[0]) // the first array will be the duration number
        startActivityForResult(intent, REQ_CODE)
    }
}

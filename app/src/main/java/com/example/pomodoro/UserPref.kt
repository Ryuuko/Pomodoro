package com.example.pomodoro

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.*
import java.io.PrintWriter
import java.io.StringWriter


class UserPref(sharedPreference: SharedPreferences, editor: SharedPreferences.Editor) {
    val sharedPreference = sharedPreference
    val editor = editor

    private var sessionTime = sharedPreference.getInt("sessionTime", 22) // default: 25 mins
    private var calendartrigger = sharedPreference.getBoolean("calendartrigger", false) // defalut as false
    private var defaultaim = sharedPreference.getInt("defaultAim", 2) // if there's no record, return 2 as default
    private var progress = 0f
    private lateinit var progressConverter: ProgressConverter
    var savedsound = "null"
    lateinit var mp: MediaPlayer

    /* Sound Setup*/
    fun soundSetup(activity: Activity, soundtype: String, soundicon: ImageView){

        savedsound = sharedPreference.getString(soundtype, "null") // defalut as dial 0

        when(savedsound!="null"){
            false -> {soundIcon(activity, soundtype, soundicon, "light")}
            true -> {soundIcon(activity, soundtype, soundicon, "dark")
                if(soundtype=="musicsound") {
                    val selected = savedsound.toLowerCase()
                    soundPlayer(activity, selected, soundtype)
                }
            }
        }
        soundicon.setOnClickListener{
            Log.d("hey", savedsound)
            when(savedsound!="null"){
                false -> {soundIcon(activity, soundtype, soundicon, "dark")
                dialogsound(activity, soundtype, soundicon)}
                true -> {soundIcon(activity, soundtype, soundicon, "light")
                    releaseLast()
                    savedsound = "null"
                    soundSaver(soundtype)}
            }
        }
    }

    fun dialogsound(activity: Activity, soundtype: String, soundicon: ImageView){
        val listID = activity.resources.getIdentifier(soundtype, "array", "com.example.pomodoro")
        val items = activity.resources.getStringArray(listID)
        val builder = android.app.AlertDialog.Builder(activity)

        // set title of the alert
        builder.setTitle("Sound Choice")
        // selected item will trigger the related sound
        builder.setSingleChoiceItems(items, -1,
            DialogInterface.OnClickListener { dialog, which ->
                val selected = items[which].toLowerCase()
                releaseLast()
                soundPlayer(activity, selected, soundtype)
            })
            // Add action buttons
            .setPositiveButton("Set",
                DialogInterface.OnClickListener { dialog, _->
                    val selectedPosition = (dialog as AlertDialog).listView.checkedItemPosition
                    savedsound = items[selectedPosition]
                    if(!soundtype.equals("musicsound")){
                        releaseLast()
                    } // if not playing music sound, start/end sound has no need to be played immediately
                      // if it's playing the music, then do nothing and just let the selected sound keep playing

                    soundSaver(soundtype)
                    dialog.cancel()
                })
            .setNegativeButton("Cancel",
                // the calendar will not be activated when the dialog is dismissed
                DialogInterface.OnClickListener { dialog, _->
                    soundIcon(activity, soundtype, soundicon, "light")
                    releaseLast()
                    dialog.cancel()
                })
        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(false) // to make sure the icon will not "light up" if the value isn't set
        dialog.show()
    }

    // handle icon mechanism
    fun soundIcon(activity: Activity, soundtype: String, soundicon: ImageView, color: String){
        Log.d("iconName", soundtype+color)
        val drawableID = activity.resources.getIdentifier(soundtype+color, "drawable",
            "com.example.pomodoro")
        soundicon.setImageResource(drawableID)
    }

    // play the sound
    fun soundPlayer(activity: Activity, selected: String, soundtype: String) {
        val ID = activity.resources.getIdentifier(selected, "raw", "com.example.pomodoro")
        mp = MediaPlayer.create(activity, ID)
        mp.start()
        if(soundtype=="musicsound"){mp.isLooping=true}
    }

    // stop and release the last played media player if it's initialized
    fun releaseLast(){
        try{
            if(this::mp.isInitialized){
            if(mp.isPlaying){
                mp.stop() }  // using stop may has crash if we stop some that's already compete?
            mp.release() }
        }
            catch(e: IllegalStateException){ // when will reach IllegalStateException?
                val sw = StringWriter()
                e.printStackTrace(PrintWriter(sw))
                val exceptionAsString = sw.toString()
                println(exceptionAsString)
            }
    }

    fun soundSaver(soundtype: String){
        editor.putString(soundtype, savedsound)
        Log.d("saveSound", "$savedsound is collected for $soundtype")
        editor.commit()
    }

    /* Calendar Setup*/
    fun calendarSetup(activity: Activity){

        val calendar = activity.findViewById<ImageView>(R.id.planbutton)

        // default icon setup
        when(calendartrigger){
            false -> {calendar.setImageResource(R.drawable.calendarlight)}
            true -> {calendar.setImageResource(R.drawable.calendardark)}
        }

        calendar.setOnClickListener{
            when(calendartrigger){
                false -> {calendar.setImageResource(R.drawable.calendardark); calendartrigger = true;
                    dialogSetup(activity)}
                true -> {calendar.setImageResource(R.drawable.calendarlight); calendartrigger = false;
                    progressDisable(activity)
                    }
            }
            editor.putBoolean("calendartrigger", calendartrigger)
            editor.commit()
        }
    }

    fun dialogSetup(activity: Activity){
        val npView = activity.layoutInflater.inflate(R.layout.dialog, null)
        val numberPicker = npView.findViewById<NumberPicker>(R.id.numberPicker)

        // set up the picker for aim duration
        numberPicker.maxValue = 6
        numberPicker.minValue = 1
        numberPicker.value = defaultaim

        val builder = android.app.AlertDialog.Builder(activity)
        // set title of the alert
        builder.setTitle("Set Your Goal")
        builder.setMessage("How many hours do you want to work per day?\n\n " +
                "(Good News! A traditional Pomodoro 25 mins will be treated as 30 mins here!)")
        builder.setView(npView)
            // Add action buttons
            .setPositiveButton("Set",
                DialogInterface.OnClickListener { dialog, _->
                    defaultaim = numberPicker.value
                    editor.putInt("defaultAim", defaultaim)
                    editor.commit()
                    aimReload(defaultaim, activity)
                    dialog.cancel()
                })
            .setNegativeButton("Cancel",
                // the calendar will not be activated when the dialog is dismissed
                DialogInterface.OnClickListener { dialog, _->
                val calendar = activity.findViewById<ImageView>(R.id.planbutton)
                calendar.setImageResource(R.drawable.calendarlight)
                calendartrigger = false
                dialog.cancel()
            })
        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(false) // to make sure the icon will not "light up" if the value isn't set
        dialog.show()
    }

    fun aimReload(defaultAim: Int, activity: Activity){
        val runTime = sharedPreference.getInt("runTime", 2)
        progressConverter = ProgressConverter(runTime) // todo:call it in the progressSetup to match the logic
        progress = progressConverter.progressCal(defaultAim)
        progressSetup(progress, activity)
    }

    fun progressSetup(progress: Float, activity: Activity){
        // todo: total variable is just stupid! just directly call progressConverter
        val review = activity.findViewById<TextView>(R.id.progressreview)
        review.visibility = VISIBLE
            when{
                progress<100f -> review.setText(
                        "${progress.toInt()}% of progress has been completed")
                progress==100f -> review.setText(
                        "Congratulations! You've completed today's requirement! Take a rest!")
                progress>100f -> review.setText(
                        "Wow! ${progress.toInt()}% has been made!")
            }
    }

    fun progressDisable(activity: Activity){
        activity.findViewById<TextView>(R.id.progressreview).visibility = GONE
    }

    fun savedsound(): String{
        return savedsound
    }

    fun sessionsave(newsession: Int){
        editor.putInt("sessionTime", newsession)
        editor.commit()
    }

    fun defaultaim(): Int{
        return defaultaim
    }

    fun progress(): Float{
        return progress
    }

    fun session(): Int{
        return sessionTime
    }

    fun calendartrigger(): Boolean{
        return calendartrigger
    }
}
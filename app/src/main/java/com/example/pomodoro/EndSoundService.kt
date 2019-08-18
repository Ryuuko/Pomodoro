package com.example.pomodoro

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.util.Log

class EndSoundService : Service() {
    lateinit var mpEndSound: MediaPlayer
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if(intent!=null){
            val selected = intent.getStringExtra("selected")
            Log.d("heyyy", "I've received the end sound, that's $selected")
            endSoundPlay(selected)
        }

        return START_NOT_STICKY // using START_STICKY will restart the service even the it's once stopped
    }

    private fun endSoundPlay(selected: String){
        Log.d("hey", selected)
        val ID = this.resources.getIdentifier(selected,
            "raw", "com.example.pomodoro")
        val isInitial = this::mpEndSound.isInitialized
        Log.d("isInitial", "mp is $isInitial, the selected sound is $selected")
        mpEndSound = MediaPlayer.create(this, ID)
        mpEndSound.start()
        mpEndSound.setOnCompletionListener {
            mpEndSound.release() // release the object since start sound will be not used anymore
            this.stopSelf() // once the sound is completed we can close the service, the additional service will not
        }
    }
    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}

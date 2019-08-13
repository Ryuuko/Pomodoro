package com.example.pomodoro

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.koushikdutta.ion.Ion
import kotlinx.android.synthetic.main.activity_welcome.*
import kotlinx.android.synthetic.main.activity_working.*
import org.json.JSONObject
import android.net.NetworkInfo
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import kotlin.concurrent.thread


class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // enable full screen
        try { this.supportActionBar!!.hide()
        } catch (e: NullPointerException) { }

        setContentView(R.layout.activity_welcome)

            YoYo.with(Techniques.Flash)
                .duration(4000)
                .repeat(1000)
                .playOn(tapReminder);

        if(isNetworkAvailable()){quoteFetch()}

        welcomeLayout.setOnClickListener{
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            //todo ActivityManager: Process com.example.pomodoro (pid 10340) has died: vis  +99TOP  wtf???
        }
    }
    fun quoteFetch(){
        Ion.with(this)
            .load("https://goodquotesapi.herokuapp.com/tag/inspiration") // todo: make a random page if neccessary
            .asString()
            .setCallback { _, result ->
                processQuote(result)
            }
    }

    /* quotes format: */
//    {
//        "current_page": 3,
//        "total_pages": 100,
//        "quotes": [
//        {
//            "quote": "It's not the load that breaks you down, it's the way you carry it.",
//            "author": "\n    Lou Holtz\n  ",
//            "publication": null
//        }

    fun processQuote(result: String){

        val json = JSONObject(result)
        val resultsArray = json.getJSONArray("quotes")

        // get a random quote
        // Math.random() generate number from [0, 1), then it will never generate number
        // equal or greater than 1, which will cause the randomInt equal to or greater than the array length

        var randomDouble = Math.random() * resultsArray.length()
        val randomInt = randomDouble.toInt()

        Log.d("random", resultsArray.length().toString())
        Log.d("random", randomInt.toString())

        val quote = resultsArray.getJSONObject(randomInt).getString("quote")

        // get and form the author's name
        val authorString = resultsArray.getJSONObject(randomInt).getString("author")
        val regex = Regex("([A-Z]\\w+)")
        val matches = regex.findAll(authorString)
        val name = matches.map { it.groupValues[1] }.joinToString()
        val nameFormed = "─" + name.replace(",", "")

        display(quote, nameFormed)
    }

    fun display(words: String, author: String){
        quote.setText(words)
        authorName.setText(author)
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }
}

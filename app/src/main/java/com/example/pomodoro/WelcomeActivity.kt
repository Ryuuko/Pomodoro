package com.example.pomodoro

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import kotlinx.android.synthetic.main.activity_welcome.*
import org.json.JSONObject
import android.os.SystemClock
import android.view.View.VISIBLE
import android.widget.TextView
import java.io.BufferedReader
import java.io.InputStream
import kotlin.concurrent.thread


class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // enable full screen
        try { this.supportActionBar!!.hide()
        } catch (e: NullPointerException) { }

        setContentView(R.layout.activity_welcome)

        welcomeLayout.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        quoteFetch()

    }

    private fun quoteFetch(){
        // read the json as String
            var json: String? = null
            try {
                // geting the data from the internal storage
                val inputStream: InputStream = resources.openRawResource(R.raw.quotes)
                // read the line by line and pack all the data into the same josn string
                json = inputStream.bufferedReader().use{it.readText()}
                processQuote(json)
            } catch (e: Exception) {
                default()
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

    private fun processQuote(result: String){

        Log.d("heyyy", "it's being processed")
        val json = JSONObject(result)
        val resultsArray = json.getJSONArray("quotes")

        // get a random quote
        // Math.random() generate number from [0, 1), then it will never generate number
        // equal or greater than 1, which will cause the randomInt equal to or greater than the array length

        val randomDouble = Math.random() * resultsArray.length()
        val randomInt = randomDouble.toInt()

        Log.d("random", resultsArray.length().toString())
        Log.d("random", randomInt.toString())

        val quote = resultsArray.getJSONObject(randomInt).getString("quote")

        // get and form the author's name
        val authorString = resultsArray.getJSONObject(randomInt).getString("author")
        val regex = Regex("([A-Z]\\w+)")
        val matches = regex.findAll(authorString)
        val name = matches.map { it.groupValues[1] }.joinToString()

        if(name=="Pablo"){
            Log.d("name!", name)
            // skip the problematic data without using the internet again --- by using the default stuff
            default()
        } else{
            Log.d("name!", name)
            val nameFormed = "─" + name.replace(",", "")
            display(quote, nameFormed)
        }

    }

    private fun default(){
        val quote = "If you're going through hell, keep going."
        val author = "─Winston Churchill"
        display(quote, author)
    }

    private fun display(words: String, author: String){
        quote.text = words
        quoteAnimate(quote)
        authorName.text = author
        quoteAnimate(authorName)

        // Runs the specified action on the UI thread with delay of 1.5 seocnd

        thread(start=true){
            SystemClock.sleep(1500)
            runOnUiThread {

                tapReminder.visibility = VISIBLE

                YoYo.with(Techniques.Flash)
                    .duration(4000)
                    .repeat(1000)
                    .playOn(tapReminder)
            }
        }
    }

    private fun quoteAnimate(text: TextView){
        YoYo.with(Techniques.FadeIn)
            .duration(1000)
            .repeat(0)
            .playOn(text)
    }
}

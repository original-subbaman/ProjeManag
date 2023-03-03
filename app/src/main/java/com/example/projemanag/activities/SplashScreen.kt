package com.example.projemanag.activities

import android.content.Intent
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import com.example.projemanag.R
import com.example.projemanag.firebase.MyFirestore
import com.example.projemanag.utility.Utility

class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        Utility.setFullScreen(window)
        setTypeFace()

        Handler(Looper.myLooper()!!).postDelayed(
            {
                var currentUserId = MyFirestore().getCurrentUserId()
                if(currentUserId.isNotEmpty()){
                    startActivity(Intent(this, MainActivity::class.java))
                }else{
                    startActivity(Intent(this, IntroActivity::class.java))
                }
                finish()
            }, 2500
        )

    }


    private fun setTypeFace(){
        val typeFace: Typeface = Typeface.createFromAsset(assets, "MartianMono-Regular.ttf")
        findViewById<TextView>(R.id.splashScreenTitleTextView).typeface = typeFace
    }
}
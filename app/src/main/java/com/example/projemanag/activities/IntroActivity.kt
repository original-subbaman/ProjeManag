package com.example.projemanag.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import com.example.projemanag.R
import com.example.projemanag.utility.Utility

class IntroActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        Utility.setFullScreen(window)
        setSignUpButtonOnClick()
        setSignInButtonClick()
    }

    private fun setSignUpButtonOnClick(){
        findViewById<Button>(R.id.btn_sign_up_intro).setOnClickListener{
            startActivity(Intent(this@IntroActivity, SignUpActivity::class.java))
        }
    }

    private fun setSignInButtonClick(){
        findViewById<Button>(R.id.btn_sign_in_intro).setOnClickListener {
            startActivity(Intent(this@IntroActivity, SignInActivity::class.java))
        }
    }
}
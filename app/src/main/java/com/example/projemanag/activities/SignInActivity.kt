package com.example.projemanag.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.example.projemanag.R
import com.example.projemanag.model.User
import com.example.projemanag.utility.Utility
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        Utility.setFullScreen(window)
        setUpActionBar()
        setOnClickForSignInBtn()
    }

    private fun setUpActionBar(){
        setSupportActionBar(findViewById(R.id.toolbar_sign_in_activity))
        val actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back)
        }
        findViewById<Toolbar>(R.id.toolbar_sign_in_activity).setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setOnClickForSignInBtn(){
        findViewById<Button>(R.id.btn_sign_in).setOnClickListener {
            val email = trimInput(
                findViewById<TextView>(R.id.et_email_sign_in).text.toString()
            )
            val password = trimInput(
                findViewById<TextView>(R.id.et_password_sign_in).text.toString()
            )
            signInUser(email, password)
        }
    }

    private fun signInUser(email: String, password: String){
        if(checkUserInputIsNotEmpty(email, password)){
            showProgressDialog(resources.getString(R.string.please_wait))
            FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(email, password)
                .addOnCompleteListener{
                    task ->
                    hideProgressDialog()
                    if(task.isSuccessful){
                        Toast.makeText(this, "Successfully logged in", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                    }else{
                        Toast.makeText(this, "Error logging in", Toast.LENGTH_SHORT).show()
                    }
                }
        }

    }

    private fun checkUserInputIsNotEmpty(email: String, password: String): Boolean{
        return when{
            TextUtils.isEmpty(email) -> {
                showErrorSnackBar(resources.getString(R.string.please_enter_your_email))
                false
            }
            TextUtils.isEmpty(password) -> {
                showErrorSnackBar(resources.getString(R.string.please_enter_your_password))
                false
            }
            else -> true
        }
    }

    fun signInSuccess(loggedInUser: User) {
        hideProgressDialog()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
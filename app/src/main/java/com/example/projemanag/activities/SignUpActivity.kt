package com.example.projemanag.activities

import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.example.projemanag.R
import com.example.projemanag.firebase.MyFirestore
import com.example.projemanag.model.User
import com.example.projemanag.utility.Utility
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class SignUpActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        Utility.setFullScreen(window)
        setUpActionBar()
        setUpSignUpButtonOnClick()
    }

    private fun setUpActionBar(){
        setSupportActionBar(findViewById(R.id.toolbar_sign_up_activity))
        val actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back)
        }
        findViewById<Toolbar>(R.id.toolbar_sign_up_activity).setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setUpSignUpButtonOnClick(){
        findViewById<Button>(R.id.btn_sign_up).setOnClickListener {
            val name: String = trimInput(
                findViewById<TextView>(R.id.et_name).text.toString()
            )
            val email: String = trimInput(
                findViewById<TextView>(R.id.et_email_sign_in).text.toString()
            )
            val password: String = trimInput(
                findViewById<TextView>(R.id.et_password_sign_in).text.toString()
            )
            registerUser(name, email, password)
        }
    }

    private fun registerUser(name: String, email: String, password: String){
        if(validateForm(name, email, password)){
            showProgressDialog(resources.getString(R.string.please_wait))
            FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    task ->
                    if(task.isSuccessful){
                        val firebaseUser: FirebaseUser = task.result!!.user!!
                        val registeredEmail = firebaseUser.email!!
                        val user = User(firebaseUser.uid, name, registeredEmail)
                        MyFirestore().registerUser(this, user)
                    }else{
                        Toast.makeText(this, resources.getString(R.string.registration_failed), Toast.LENGTH_SHORT).show()
                    }
                }
        }

    }



    private fun validateForm(name: String,
                             email: String, password: String): Boolean{
        return when{
            TextUtils.isEmpty(name) -> {
                showErrorSnackBar("Please enter a name")
                false
            }
            TextUtils.isEmpty(email) -> {
                showErrorSnackBar("Please enter an email id")
                false
            }
            TextUtils.isEmpty(password) -> {
                showErrorSnackBar("Please enter your password")
                false
            }
            else->{
                true
            }
        }
    }

    fun userRegisteredSuccess() {
        Toast.makeText(this, "You have successfully registered", Toast.LENGTH_SHORT).show()
        hideProgressDialog()
        finish()
    }

}
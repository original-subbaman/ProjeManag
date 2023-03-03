package com.example.projemanag.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.projemanag.R
import com.example.projemanag.firebase.MyFirestore
import com.example.projemanag.model.User
import com.example.projemanag.utility.Constants
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException
import java.util.*

class MyProfileActivity : BaseActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showImageChooser()
        }
    }

    private val imageChooserLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            if (result.data != null) {
                selectedImageFileURI = Objects.requireNonNull(result.data)?.data
                try {
                    Glide.with(this@MyProfileActivity)
                        .load(selectedImageFileURI)
                        .centerCrop()
                        .placeholder(R.drawable.ic_user_place_holder)
                        .into(findViewById(R.id.iv_profile_user_image))
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private var selectedImageFileURI: Uri? = null
    private lateinit var userDetails: User
    private var profileImageUrl: String? = null


    private var imageViewProfile: ImageView? = null
    private var userNameTextView: TextView? = null
    private var emailTextView: TextView? = null
    private var mobileTextView: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)
        setViewIds()
        MyFirestore().loadUserData(this@MyProfileActivity)

        setImageViewOnClickListener()
        setUpdateButtonOnClickListener()
        setUpActionBar(R.id.toolbar_my_profile_activity, resources.getString(R.string.my_profile_title))
    }


    private fun setViewIds(){
        imageViewProfile = findViewById(R.id.iv_profile_user_image)
        userNameTextView = findViewById(R.id.et_name)
        emailTextView = findViewById(R.id.et_email)
        mobileTextView = findViewById(R.id.et_mobile)
    }

    private fun setUpdateButtonOnClickListener() {
        val updateBtn = findViewById<Button>(R.id.btn_update)
        updateBtn.setOnClickListener {
            if (selectedImageFileURI != null) {
                uploadUserImage()
            }else{
                showProgressDialog(resources.getString(R.string.please_wait))
                updateUserProfileData()
                hideProgressDialog()
            }
            setResult(RESULT_OK)
            finish()
        }
    }

    private fun setImageViewOnClickListener(){
        imageViewProfile?.setOnClickListener {
            checkReadExternalStoragePermission()
        }
    }

    fun setUserDataInUI(user: User) {
        userDetails = user
        Glide
            .with(this@MyProfileActivity)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(findViewById(R.id.iv_profile_user_image))
        findViewById<TextView>(R.id.et_name).text = user.name
        findViewById<TextView>(R.id.et_email).text = user.email
        findViewById<TextView>(R.id.et_mobile).text = user.mobile
    }

    private fun updateUserProfileData(){
        val userMap = HashMap<String, Any>()
        val name = userNameTextView?.text.toString()
        val email = emailTextView?.text.toString()
        val mobile = mobileTextView?.text.toString()

        var anyChangesMade = false
        if(profileImageUrl?.isNotEmpty() == true && (profileImageUrl != userDetails.image)){
            userMap[Constants.IMAGE] = profileImageUrl!!
            anyChangesMade = true
        }

        if(name.isNotEmpty() && name != userDetails.name){
           userMap[Constants.NAME] = name
            anyChangesMade = true
        }

        if(email.isNotEmpty() && email != userDetails.email){
            userMap[Constants.EMAIL] = email
            anyChangesMade = true
        }

        if(mobile.isNotEmpty() && mobile != userDetails.mobile){
           userMap[Constants.MOBILE] = mobile
            anyChangesMade = true
        }

        if(anyChangesMade) MyFirestore().updateUserProfileData(this@MyProfileActivity, userMap)
    }

    private fun checkReadExternalStoragePermission() {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                this@MyProfileActivity,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) -> {
                showImageChooser()
            }
            else -> {
                requestPermissionLauncher.launch(
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            }
        }
    }

    private fun showImageChooser() {
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        imageChooserLauncher.launch(galleryIntent)
    }

    private fun getFileExtension(uri: Uri?): String? {
        return MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(contentResolver.getType(uri!!))
    }

    private fun uploadUserImage() {
        showProgressDialog(resources.getString(R.string.please_wait))
        if (selectedImageFileURI != null) {
            val sRef: StorageReference = FirebaseStorage
                .getInstance()
                .reference
                .child(
                    "USER_IMAGE"
                            + System.currentTimeMillis()
                            + "."
                            + getFileExtension(selectedImageFileURI)
                )

            sRef.putFile(selectedImageFileURI!!)
                .addOnSuccessListener { taskSnapshot ->
                    Log.i("Profile", taskSnapshot.metadata!!.reference!!.downloadUrl.toString())

                    taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                        Log.i("Download Image URI", uri.toString())
                        profileImageUrl = uri.toString()
                        updateUserProfileData()
                        hideProgressDialog()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this@MyProfileActivity, e.message, Toast.LENGTH_LONG).show()
                    hideProgressDialog()
                }
        }

    }

    fun profileUpdateSuccess(){
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }
}
package com.example.projemanag.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.example.projemanag.R
import com.example.projemanag.firebase.MyFirestore
import com.example.projemanag.model.Board
import com.example.projemanag.utility.Constants
import com.google.firebase.firestore.remote.FirestoreChannel
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class CreateBoardActivity : BaseActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showImageChooser()
        } else {

        }
    }
    private val imageChooserLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            if (result.data != null) {
                selectedBoardImageURI = Objects.requireNonNull(result.data)?.data
                try {
                    setBoardImage()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }
    private var selectedBoardImageURI: Uri? = null
    private var boardImage: ImageView? = null
    private lateinit var userName: String
    private var createButton: Button? = null
    var boardImageUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_board)
        setUpActionBar(
            R.id.toolbar_create_board_activity,
            resources.getString(R.string.create_board_title)
        )

        if (intent.hasExtra(Constants.NAME)) {
            userName = intent.getStringExtra(Constants.NAME).toString()
        }
        boardImage = findViewById(R.id.iv_board_image)
        boardImage?.setOnClickListener {
            showImageChooser()
        }

        createButton = findViewById(R.id.btn_create)
        createButton?.setOnClickListener {
            if (selectedBoardImageURI != null) {
                uploadBoardImage()
            }else{
                createBoard()
            }
        }

    }

    private fun showImageChooser() {
        var galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        imageChooserLauncher.launch(galleryIntent)
    }

    private fun setBoardImage() {
        Glide.with(this)
            .load(selectedBoardImageURI)
            .centerCrop()
            .placeholder(R.drawable.ic_baseline_circle)
            .into(findViewById(R.id.iv_board_image))
    }

    private fun createBoard() {
        val assignedUserArrayList: ArrayList<String> = ArrayList()
        assignedUserArrayList.add(getCurrentUserId())
        val boardNameText = findViewById<EditText>(R.id.et_board_name).text.toString()

        if(boardNameText.isNotEmpty()){
            showProgressDialog()
            var newBoard: Board = Board(
                boardNameText,
                boardImageUrl,
                userName,
                assignedUserArrayList
            )
            MyFirestore().createBoard(this, newBoard)
        }else{
            showErrorSnackBar(resources.getString(R.string.empty_board_name_text))
        }
    }

    private fun uploadBoardImage() {
        showProgressDialog(resources.getString(R.string.please_wait))
        val sRef: StorageReference = FirebaseStorage
            .getInstance()
            .reference
            .child(
                "BOARD_IMAGE"
                        + System.currentTimeMillis()
                        + "."
                        + Constants.getFileExtension(selectedBoardImageURI, this)
            )

        sRef.putFile(selectedBoardImageURI!!)
            .addOnSuccessListener { taskSnapshot ->
                Log.i("Board Image URL", taskSnapshot.metadata!!.reference!!.downloadUrl.toString())

                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                    Log.i("Download Image URI", uri.toString())
                    boardImageUrl = uri.toString()
                    createBoard()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
                hideProgressDialog()
            }
    }

    fun boardCreatedSuccessfully() {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }


}
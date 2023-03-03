package com.example.projemanag.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.example.projemanag.R
import com.example.projemanag.adapters.BoardRecyclerAdapter
import com.example.projemanag.firebase.MyFirestore
import com.example.projemanag.model.Board
import com.example.projemanag.model.User
import com.example.projemanag.utility.Constants
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var drawer: DrawerLayout? = null
    private var navView: NavigationView? = null
    private var fab: FloatingActionButton? = null
    private lateinit var userName: String
    private lateinit var sharedPreferences: SharedPreferences

    private val getResultFromProfileActivity = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            MyFirestore().loadUserData(this@MainActivity)
        } else {
            Log.e(javaClass.simpleName, it.resultCode.toString())
        }
    }

    private val getResultFromCreateBoardActivity = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            MyFirestore().getBoardsList(this)
        } else {

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getUpdatedFCMToken()
        setToolBar()
        drawer = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        fab = findViewById(R.id.fab_create_board)
        navView?.setNavigationItemSelectedListener(this)
        MyFirestore().loadUserData(this@MainActivity, true)
        setFabOnClickListener()
        setUpSwipeRefreshLayout()


    }


    private fun getUpdatedFCMToken() {
        sharedPreferences = this.getSharedPreferences(
            Constants.PROJEMANAG_PREFERENCES, Context.MODE_PRIVATE
        )
        val tokenUpdated = sharedPreferences
            .getBoolean(Constants.FCM_TOKEN_UPDATED, false)

        if (tokenUpdated) {
            showProgressDialog()
            MyFirestore().loadUserData(this, true)
        } else {
            updateFCMToken()
        }

    }

    private fun updateFCMToken() {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            updateFCMToken(token)
        }
    }


    private fun setToolBar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar_main_activity)
        setSupportActionBar(toolbar)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.colorPrimary)))
        toolbar.setNavigationIcon(R.drawable.ic_action_nav_menu)
        toolbar.setNavigationOnClickListener {
            toggleDrawer()
        }
    }

    private fun setFabOnClickListener() {
        fab?.setOnClickListener {
            getResultFromCreateBoardActivity.launch(
                Intent(this, CreateBoardActivity::class.java).putExtra(
                    Constants.NAME,
                    userName
                )
            )
        }
    }

    private fun setUpSwipeRefreshLayout(){
        val swipeLayout = findViewById<SwipeRefreshLayout>(R.id.boards_swipe_layout)
        swipeLayout.setOnRefreshListener {
            MyFirestore().loadUserData(this, true)
            swipeLayout.isRefreshing = false
        }
    }

    private fun toggleDrawer() {
        if (drawer!!.isDrawerOpen(GravityCompat.START)) {
            drawer!!.closeDrawer(GravityCompat.START)
        } else {
            drawer!!.openDrawer(GravityCompat.START)
        }
    }

    override fun onBackPressed() {
        if (drawer!!.isDrawerOpen(GravityCompat.START)) {
            drawer!!.closeDrawer(GravityCompat.START)
        } else {
            doubleBackToExit()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_my_profile -> {
                getResultFromProfileActivity.launch(
                    Intent(this, MyProfileActivity::class.java)
                )
            }
            R.id.nav_sign_out -> {
                FirebaseAuth.getInstance().signOut()
                sharedPreferences.edit().clear().apply()
                val intent = Intent(this, IntroActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
                finish()
            }
        }
        drawer!!.closeDrawer(GravityCompat.START)
        return true
    }

    fun updateNavigationUserDetails(loggedInUser: User, readBoardList: Boolean) {
        userName = loggedInUser.name
        Glide
            .with(this)
            .load(loggedInUser.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(findViewById(R.id.nav_user_image))
        findViewById<TextView>(R.id.nav_user_name).text = loggedInUser.name
        if (readBoardList) {
            MyFirestore().getBoardsList(this)
        }
    }


    fun populateBoardsListToUI(boardsList: ArrayList<Board>) {
        hideProgressDialog()
        val recyclerView = findViewById<RecyclerView>(R.id.rv_boards_list)

        val noBoardsAvailableTV =
            findViewById<TextView>(R.id.tv_no_boards_available)
        if (boardsList.size > 0) {

            recyclerView.visibility = View.VISIBLE
            noBoardsAvailableTV.visibility = View.GONE

            setUpRecyclerView(recyclerView, boardsList)

        } else {

            recyclerView.visibility = View.GONE
            noBoardsAvailableTV.visibility = View.VISIBLE

        }
    }

    private fun setUpRecyclerView(recyclerView: RecyclerView, boardsList: ArrayList<Board>) {

        recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
        recyclerView.setHasFixedSize(true)

        val adapter = BoardRecyclerAdapter(this, boardsList)
        recyclerView.adapter = adapter

        adapter.setOnClickListener(object : BoardRecyclerAdapter.OnClickListener {
            override fun onClick(position: Int, model: Board) {
                val taskListIntent = Intent(this@MainActivity, TaskListActivity::class.java)
                taskListIntent.putExtra(Constants.DOCUMENT_ID, model.documentId)
                startActivity(taskListIntent)
            }
        })

    }

    fun tokenUpdateSuccess() {
        hideProgressDialog()
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putBoolean(Constants.FCM_TOKEN_UPDATED, true)
        editor.apply()
        MyFirestore().loadUserData(this, true)
    }

    private fun updateFCMToken(token: String) {
        val userHashMap = HashMap<String, Any>()
        userHashMap[Constants.FCM_TOKEN] = token
        MyFirestore().updateUserProfileData(this, userHashMap)
    }
}
package com.example.projemanag.activities

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projemanag.R
import com.example.projemanag.adapters.MembersListRecyclerAdapter
import com.example.projemanag.firebase.MyFirestore
import com.example.projemanag.model.Board
import com.example.projemanag.model.User
import com.example.projemanag.utility.Constants

class MembersActivity : BaseActivity() {

    private lateinit var boardDetails: Board
    private lateinit var assignedUsers: ArrayList<User>
    private var newMembersAdded: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_members)

        if (intent.hasExtra(Constants.BOARD_DETAIL)) {
            boardDetails = intent.getParcelableExtra(Constants.BOARD_DETAIL)!!
        }

        setUpActionBar(R.id.toolbar_members_activity, resources.getString(R.string.members))
        showProgressDialog(resources.getString(R.string.please_wait))
        MyFirestore().getAssignedMembersListDetails(this, boardDetails.assignedTo)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add_members, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add_member -> {
                showDialogAddMember()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showDialogAddMember() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_search_member)
        dialog.findViewById<TextView>(R.id.tv_add).setOnClickListener {
            val email = dialog.findViewById<EditText>(R.id.et_email_search_member).text.toString()
            if (email.isNotEmpty()) {
                dialog.dismiss()
                showProgressDialog(resources.getString(R.string.please_wait))
                MyFirestore().getMemberDetails(this, email)
            } else {
                Toast.makeText(this, "Please enter an email address", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.findViewById<TextView>(R.id.tv_cancel).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    fun setUpMembersList(list: ArrayList<User>) {
        assignedUsers = list
        hideProgressDialog()
        val memberRecyclerView = findViewById<RecyclerView>(R.id.rv_members_list)
        memberRecyclerView.layoutManager = LinearLayoutManager(this)
        memberRecyclerView.setHasFixedSize(true)
        memberRecyclerView.adapter = MembersListRecyclerAdapter(this, list)
    }

    fun getMemberDetailsFromFirestore(user: User) {
        boardDetails.assignedTo.add(user.uid)
        MyFirestore().assignMemberToBoard(this, boardDetails, user)
    }

    fun addNewMemberToAssignedUsersList(user: User) {
        hideProgressDialog()
        assignedUsers.add(user)
        newMembersAdded = true
        setUpMembersList(assignedUsers)

    }

    override fun onBackPressed() {
        if (newMembersAdded) {
            setResult(Activity.RESULT_OK)
        }
        super.onBackPressed()

    }


}
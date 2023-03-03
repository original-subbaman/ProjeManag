package com.example.projemanag.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.projemanag.R
import com.example.projemanag.adapters.TaskListRecyclerAdapter
import com.example.projemanag.firebase.MyFirestore
import com.example.projemanag.model.Board
import com.example.projemanag.model.Card
import com.example.projemanag.model.Task
import com.example.projemanag.model.User
import com.example.projemanag.utility.Constants

class TaskListActivity : BaseActivity() {

    private lateinit var boardDetails: Board
    private var membersActivityForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            getBoardDetailsFromFirestore()
        } else {
            Log.e("Cancelled", "No members added")
        }
    }
    private var cardDetailActivityForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            getBoardDetailsFromFirestore()
        }
    }
    private lateinit var boardDocumentId: String
    lateinit var assignedMemberDetailList: ArrayList<User>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_list)

        if (intent.hasExtra(Constants.DOCUMENT_ID)) {
            boardDocumentId = intent.getStringExtra(Constants.DOCUMENT_ID).toString()
        }

        getBoardDetailsFromFirestore()

    }

    private fun getBoardDetailsFromFirestore() {
        showProgressDialog(resources.getString(R.string.please_wait))
        MyFirestore().getBoardDetails(this, boardDocumentId)
    }



    fun setUpBoardDetails(board: Board) {
        boardDetails = board

        hideProgressDialog()
        setUpActionBar(R.id.toolbar_task_list_activity, board.name.toString())

        MyFirestore().getAssignedMembersListDetails(
            this,
            boardDetails.assignedTo
        )
    }

    private fun setUpTaskListRecyclerView(taskList: ArrayList<Task>) {
        val recyclerView = findViewById<RecyclerView>(R.id.rv_task_list)
        recyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.setHasFixedSize(true)

        val adapter = TaskListRecyclerAdapter(this, taskList)
        recyclerView.adapter = adapter
    }

    fun addUpdateTaskListSuccess() {
        hideProgressDialog() //Hiding the progress dialog that loads the activity
        showProgressDialog(resources.getString(R.string.please_wait)) //This progress bar is for loading board details
        MyFirestore().getBoardDetails(this@TaskListActivity, boardDetails.documentId.toString())
    }

    private fun removeLastElementFromTaskList() {
        boardDetails.taskList.removeAt(boardDetails.taskList.size - 1)
    }
    private fun updateTaskListInDatabase(){
        showProgressDialog(resources.getString(R.string.please_wait))
        MyFirestore().addUpdateTaskList(this, boardDetails)
    }

    fun createTaskList(taskListName: String) {
        val task = Task(taskListName, MyFirestore().getCurrentUserId())
        boardDetails.taskList.add(0, task)
        removeLastElementFromTaskList()
        updateTaskListInDatabase()
    }

    fun updateTaskList(position: Int, listName: String, model: Task) {
        val task = Task(listName, model.createdBy)
        boardDetails.taskList[position] = task
        removeLastElementFromTaskList()
        updateTaskListInDatabase()
    }

    fun deleteTaskList(position: Int) {
        boardDetails.taskList.removeAt(position)
        removeLastElementFromTaskList()
        updateTaskListInDatabase()
    }

    fun addCardToTaskList(position: Int, cardName: String) {

        val currentUser = MyFirestore().getCurrentUserId()
        val cardAssignedUserList: ArrayList<String> = ArrayList()
        cardAssignedUserList.add(currentUser)

        val newCard = Card(cardName, currentUser, cardAssignedUserList)

        val cardsList = boardDetails.taskList[position].cards
        cardsList.add(newCard)

        val task = Task(
            boardDetails.taskList[position].title,
            boardDetails.taskList[position].createdBy,
            cardsList
        )

        boardDetails.taskList[position] = task


        removeLastElementFromTaskList()
        updateTaskListInDatabase()
    }

    fun startCardDetailsActivityWithResult(taskListPosition: Int, cardPosition: Int) {
        val intent = Intent(this, CardDetailActivity::class.java)
        intent.putExtra(Constants.BOARD_DETAIL, boardDetails)
        intent.putExtra(Constants.TASK_LIST_ITEM_POSITION, taskListPosition)
        intent.putExtra(Constants.CARD_LIST_ITEM_POSITION, cardPosition)
        intent.putExtra(Constants.BOARD_MEMBERS_LIST, assignedMemberDetailList)
        cardDetailActivityForResult.launch(intent)
    }

    fun getBoardMembersList(list: ArrayList<User>) {
        assignedMemberDetailList = list
        hideProgressDialog()
        val addTaskList = Task(resources.getString(R.string.add_list))
        boardDetails.taskList.add(addTaskList)

        setUpTaskListRecyclerView(boardDetails.taskList)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_members, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_member -> {
                val memberActivityIntent = Intent(this, MembersActivity::class.java)
                memberActivityIntent.putExtra(Constants.BOARD_DETAIL, boardDetails)
                membersActivityForResult.launch(
                    memberActivityIntent
                )
                return true
            }
            R.id.action_delete_board -> {
                showDeleteBoardDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun updateCardsInTaskList(taskListPosition: Int, cards: ArrayList<Card>){
        removeLastElementFromTaskList()
        boardDetails.taskList[taskListPosition].cards = cards
        updateTaskListInDatabase()
    }

    fun showDeleteBoardDialog(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Alert!")
        builder.setMessage(
            "Are you sure you want to delete this board?"
        )
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        builder.setPositiveButton("Yes") {
            dialog, _ ->
            dialog.dismiss()
            MyFirestore().deleteBoard(boardDetails, this@TaskListActivity)
        }

        builder.setNegativeButton("No"){
            dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(true)
        alertDialog.show()
    }

    fun finishActivity(){
        hideProgressDialog()
        finish()
    }



}
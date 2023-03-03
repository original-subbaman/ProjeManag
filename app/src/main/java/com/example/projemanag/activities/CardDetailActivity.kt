package com.example.projemanag.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projemanag.R
import com.example.projemanag.adapters.CardMemberListRecyclerAdapter
import com.example.projemanag.dialogs.LabelColorListDialog
import com.example.projemanag.dialogs.MembersListDialog
import com.example.projemanag.firebase.MyFirestore
import com.example.projemanag.model.*
import com.example.projemanag.utility.Constants
import java.text.SimpleDateFormat
import java.util.*


class CardDetailActivity : BaseActivity() {

    private lateinit var boardDetails: Board
    private lateinit var membersDetailList: ArrayList<User>
    private var taskListPosition = -1
    private var cardPosition = -1
    private var selectedColor = ""
    private var selectedDueDateMilliSeconds: Long = 0
    private lateinit var selectedMembersAdapter: CardMemberListRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_detail)

        getIntentData()

        val cardName: String = boardDetails
            .taskList[taskListPosition]
            .cards[cardPosition]
            .name
        val etNameCardDetail = findViewById<EditText>(R.id.et_name_card_details)

        setDueDateText()
        setUpActionBar(R.id.toolbar_card_details_activity, cardName)

        etNameCardDetail.setText(cardName)
        etNameCardDetail.setSelection(etNameCardDetail.text.toString().length)

        setOnClickForUpdateButton()
        setOnClickForLabelColorTextView()
        setOnClickForSelectMembersTextView()
        setOnClickForDueDateTextView()

        selectedColor = boardDetails.taskList[taskListPosition].cards[cardPosition].labelColor
        if (selectedColor.isNotEmpty()) {
            setColors()
        }

        setUpSelectedMembersList()

    }

    private fun getIntentData() {
        if (intent.hasExtra(Constants.BOARD_DETAIL)) {
            boardDetails = intent.getParcelableExtra<Board>(Constants.BOARD_DETAIL)!!
        }

        if (intent.hasExtra(Constants.TASK_LIST_ITEM_POSITION)) {
            taskListPosition = intent.getIntExtra(Constants.TASK_LIST_ITEM_POSITION, -1)
        }

        if (intent.hasExtra(Constants.CARD_LIST_ITEM_POSITION)) {
            cardPosition = intent.getIntExtra(Constants.CARD_LIST_ITEM_POSITION, -1)
        }

        if (intent.hasExtra(Constants.BOARD_MEMBERS_LIST)) {
            membersDetailList = intent
                .getParcelableArrayListExtra(Constants.BOARD_MEMBERS_LIST)!!
        }
    }

    private fun setDueDateText() {
        selectedDueDateMilliSeconds = boardDetails
            .taskList[taskListPosition]
            .cards[cardPosition]
            .dueDate

        if(selectedDueDateMilliSeconds > 0) {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
            val selectedDate = sdf.format(Date(selectedDueDateMilliSeconds))
            findViewById<TextView>(R.id.tv_select_due_date).text = selectedDate
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_delete_card, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_delete_card -> {
                alertDialogForDeleteCard()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun addUpdateTaskListSuccess() {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun setOnClickForUpdateButton() {
        findViewById<Button>(R.id.btn_update_card_details).setOnClickListener {
            if (findViewById<EditText>(R.id.et_name_card_details)
                    .text
                    .toString()
                    .isNotEmpty()
            ) {
                updateCardDetails()
            } else {
                Toast.makeText(this, "Enter a card name", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setOnClickForSelectMembersTextView() {
        findViewById<TextView>(R.id.tv_select_members).setOnClickListener {
            retrieveMembersAssignedToCards()
            showDialogForMembersList()
        }
    }

    private fun setOnClickForDueDateTextView(){
        findViewById<TextView>(R.id.tv_select_due_date).setOnClickListener {
            showDatePicker()
        }
    }

    private fun retrieveMembersAssignedToCards() {
        val cardAssignedMembers = boardDetails.taskList[taskListPosition]
            .cards[cardPosition].assignedTo
        if (cardAssignedMembers.size > 0) {
            for (i in membersDetailList.indices) {
                for (j in cardAssignedMembers) {
                    if (membersDetailList[i].uid == j) {
                        membersDetailList[i].selected = true
                    }
                }
            }
        } else {
            for (i in membersDetailList.indices) {
                membersDetailList[i].selected = false
            }
        }
    }

    private fun showDialogForMembersList() {
        retrieveMembersAssignedToCards()
        val listDialog = object : MembersListDialog(
            this,
            membersDetailList,
            resources.getString(R.string.str_select_member)
        ) {
            override fun onItemSelected(user: User, action: String) {
                val assignedTo = boardDetails
                    .taskList[taskListPosition]
                    .cards[cardPosition]
                    .assignedTo
                if (action == Constants.SELECT) {
                    if (!assignedTo.contains(user.uid)) {
                        assignedTo.add(user.uid)
                        Log.e("string", "assinged to $assignedTo")
                    }
                } else {
                    assignedTo.remove(user.uid)
                    for (i in membersDetailList.indices) {
                        if (membersDetailList[i].uid == user.uid) membersDetailList[i].selected =
                            false
                    }
                }
                setUpSelectedMembersList()
            }
        }
        listDialog.show()
    }

    private fun setOnClickForLabelColorTextView() {
        findViewById<TextView>(R.id.tv_select_label_color).setOnClickListener {
            createLabelColorsListDialog()
        }
    }

    private fun setUpSelectedMembersList() {
        val cardAssignedMembersList = boardDetails
            .taskList[cardPosition]
            .cards[cardPosition]
            .assignedTo

        val selectedMemberList: ArrayList<SelectedMember> = ArrayList()

        for (i in membersDetailList.indices) {
            for (j in cardAssignedMembersList) {
                if (membersDetailList[i].uid == j) {
                    val selectedMember = SelectedMember(
                        membersDetailList[i].uid,
                        membersDetailList[i].image
                    )

                    Log.e("this", membersDetailList[i].image)
                    selectedMemberList.add(selectedMember)
                }
            }
        }

        val selectMembersTV = findViewById<TextView>(R.id.tv_select_members)
        if (selectedMemberList.size > 0) {
            selectedMemberList.add(SelectedMember("", ""))
            selectMembersTV.visibility = View.GONE
            setUpRecyclerViewForSelectedMembers(selectedMemberList)
        } else {
            selectMembersTV.visibility = View.VISIBLE
            findViewById<RecyclerView>(R.id.rv_selected_members_list).visibility = View.GONE
        }
    }
    private fun show_children(v: View) {
        val viewgroup = v as ViewGroup
        for (i in 0 until viewgroup.childCount) {
            val v1 = viewgroup.getChildAt(i)
            (v1 as? ViewGroup)?.let { show_children(it) }
            Log.d("APPNAME", v1.toString())
        }
    }
    private fun setUpRecyclerViewForSelectedMembers(list: ArrayList<SelectedMember>) {
        val recyclerViewSelectedMembers = findViewById<RecyclerView>(R.id.rv_selected_members_list)
        if(recyclerViewSelectedMembers.adapter != null){
            (recyclerViewSelectedMembers.adapter as CardMemberListRecyclerAdapter).updateList(list)
            (recyclerViewSelectedMembers.adapter as CardMemberListRecyclerAdapter).notifyDataSetChanged()
            return
        }
        recyclerViewSelectedMembers.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )

        for(i in list){
            Log.e("Selectedmembers", "$i")
        }

        selectedMembersAdapter = CardMemberListRecyclerAdapter(
            this@CardDetailActivity,
            list,
            true
        )

        recyclerViewSelectedMembers.adapter = selectedMembersAdapter
        selectedMembersAdapter.setOnClickListener(
            object : CardMemberListRecyclerAdapter.OnClickListener {
                override fun onClick() {
                    showDialogForMembersList()
                }
            }
        )

        recyclerViewSelectedMembers.visibility = View.VISIBLE
    }

    private fun updateRecyclerViewListForSelectedMembers(list: ArrayList<SelectedMember>){

    }

    private fun updateCardDetails() {
        val card = Card(
            findViewById<EditText>(R.id.et_name_card_details).text.toString(),
            boardDetails.taskList[taskListPosition].cards[cardPosition].createdBy,
            boardDetails.taskList[taskListPosition].cards[cardPosition].assignedTo,
            selectedColor,
            selectedDueDateMilliSeconds
        )

        val taskList: ArrayList<Task> = boardDetails.taskList
        taskList.removeAt(taskList.size - 1)

        boardDetails.taskList[taskListPosition].cards[cardPosition] = card
        updateDatabaseWithLatestChanges()
    }

    private fun updateDatabaseWithLatestChanges() {
        showProgressDialog(resources.getString(R.string.please_wait))
        MyFirestore().addUpdateTaskList(this@CardDetailActivity, boardDetails)
    }

    private fun alertDialogForDeleteCard() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(resources.getString(R.string.alert))
        builder.setMessage(
            resources.getString(
                R.string.confirmation_message_to_delete_card,
            )
        )
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        builder.setPositiveButton(resources.getString(R.string.yes)) { dialog, _ ->
            dialog.dismiss()
            deleteCard()
        }
        builder.setNegativeButton(resources.getString(R.string.no)) { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun deleteCard() {
        val cardsList: ArrayList<Card> = boardDetails
            .taskList[taskListPosition].cards
        cardsList.removeAt(cardPosition)

        val taskList: ArrayList<Task> = boardDetails.taskList
        taskList.removeAt(taskList.size - 1)
        taskList[taskListPosition].cards = cardsList

        updateDatabaseWithLatestChanges()
    }

    private fun colorsList(): ArrayList<String> {
        val colorsList: ArrayList<String> = ArrayList()
        colorsList.add("#43C86F")
        colorsList.add("#0C90F1")
        colorsList.add("#F72400")
        colorsList.add("#7A8089")
        colorsList.add("#D57C1D")
        colorsList.add("#770000")
        colorsList.add("#0022F8")
        return colorsList
    }

    private fun setColors() {
        val colorTextView = findViewById<TextView>(R.id.tv_select_label_color)
        colorTextView.text = ""
        colorTextView.setBackgroundColor(Color.parseColor(selectedColor))
    }

    private fun createLabelColorsListDialog() {
        val colorsList: ArrayList<String> = colorsList()
        val listDialog = object : LabelColorListDialog(
            this@CardDetailActivity,
            colorsList,
            resources.getString(R.string.str_select_label_color),
            selectedColor
        ) {
            override fun onItemSelected(color: String) {
                selectedColor = color
                setColors()
            }
        }

        listDialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun showDatePicker(){
        val calendar = Calendar.getInstance()
        val year =
            calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener{
                view, year, month, dayOfMonth ->
                val sDayOfMonth = if(dayOfMonth < 10) "0$dayOfMonth" else "$dayOfMonth"
                val sMonthOfyear = if((month + 1 ) < 10) "0$dayOfMonth" else "${month + 1}"

                val selectedDates = "$sDayOfMonth/$sMonthOfyear/$year"
                findViewById<TextView>(R.id.tv_select_due_date).text = selectedDates

                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
                val theDate = sdf.parse(selectedDates)
                selectedDueDateMilliSeconds = theDate!!.time
            },
            year,
            month,
            day
        )

        datePicker.show()
    }


}
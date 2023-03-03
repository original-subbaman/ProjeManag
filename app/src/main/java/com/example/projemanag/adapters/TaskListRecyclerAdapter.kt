package com.example.projemanag.adapters

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.projemanag.R
import com.example.projemanag.activities.TaskListActivity
import com.example.projemanag.model.Task
import java.util.*
import kotlin.collections.ArrayList

open class TaskListRecyclerAdapter(
    private val context: Context,
    private var list: ArrayList<Task>
) :
    RecyclerView.Adapter<ViewHolder>() {

    private var positionDraggedFrom = -1
    private var positionDraggedTo = -1
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_task, parent, false)
        val layoutParams = LinearLayout.LayoutParams(
            (parent.width * .7).toInt(), LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(15.toDP().toPX(), 0, (40.toDP().toPX()), 0)
        view.layoutParams = layoutParams
        return TaskListViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        @SuppressLint("RecyclerView") position: Int
    ) {
        val model = list[position]
        if (holder is TaskListViewHolder) {
            val taskListLL = holder.itemView.findViewById<LinearLayout>(R.id.ll_task_item)
            val addTaskTV = holder.itemView.findViewById<TextView>(R.id.tv_add_task_list)
            setVisibilityForTaskList(taskListLL, addTaskTV, position)

            holder.itemView.findViewById<TextView>(R.id.tv_task_list_title).text = model.title
            setOnClickListenerForButtons(holder, position, model)

            val cardRecyclerView = holder.itemView.findViewById<RecyclerView>(R.id.rv_card_list)
            cardRecyclerView.layoutManager = LinearLayoutManager(context)
            cardRecyclerView.setHasFixedSize(true)

            val adapter = CardListRecyclerAdapter(context, model.cards)
            cardRecyclerView.adapter = adapter
            adapter.setOnClickListener(
                object : CardListRecyclerAdapter.OnClickListener {
                    override fun onClick(cardPosition: Int) {
                        if (context is TaskListActivity) {
                            context.startCardDetailsActivityWithResult(position, cardPosition)
                        }
                    }
                }
            )
            setItemTouchHelper(holder, position, adapter)
        }
    }

    private fun setItemTouchHelper(holder: TaskListViewHolder, position: Int, adapter: CardListRecyclerAdapter){

        val dividerItemDecoration = DividerItemDecoration(
            context,
            DividerItemDecoration.VERTICAL
        )
        holder.itemView.findViewById<RecyclerView>(R.id.rv_card_list).addItemDecoration(
            dividerItemDecoration
        )

        val helper = ItemTouchHelper(
            object : ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: ViewHolder,
                    target: ViewHolder
                ): Boolean {
                    val dragPosition = viewHolder.adapterPosition
                    val targetPosition = target.adapterPosition
                    if(positionDraggedFrom == -1){
                        positionDraggedFrom = dragPosition
                    }
                    positionDraggedTo = targetPosition
                    Collections.swap(list[position].cards, dragPosition, targetPosition)
                    adapter.notifyItemMoved(dragPosition, targetPosition)
                    return false
                }

                override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
                    TODO("Not yet implemented")
                }

                override fun clearView(recyclerView: RecyclerView, viewHolder: ViewHolder) {
                    super.clearView(recyclerView, viewHolder)
                    if(positionDraggedFrom != -1 && positionDraggedTo != -1 &&
                        positionDraggedTo != positionDraggedFrom){
                        (context as TaskListActivity).updateCardsInTaskList(position, list[position].cards)
                        positionDraggedFrom = -1
                        positionDraggedTo = -1
                    }
                }

            }
        )

        helper.attachToRecyclerView(holder.itemView.findViewById(R.id.rv_card_list))

    }

    private fun setVisibilityForTaskList(
        taskListLinearLayout: LinearLayout,
        addTaskTextView: TextView,
        position: Int
    ) {

        if (position == list.size - 1) {
            addTaskTextView.visibility = View.VISIBLE
            taskListLinearLayout.visibility = View.GONE
        } else {
            addTaskTextView.visibility = View.GONE
            taskListLinearLayout.visibility = View.VISIBLE
        }

    }

    private fun setOnClickListenerForButtons(holder: ViewHolder, position: Int, model: Task) {
        setOnClickForTaskListTV(holder)

        setOnClickForCloseListImageButton(holder)

        setOnClickForDoneListImageButton(holder)

        setOnClickForEditImageButton(holder, model)

        setOnClickForCloseEditableImageButton(holder)

        setOnClickForDoneEditingImageButton(holder, position, model)

        setOnClickForDeleteImageButton(holder, position, model.title)

        setOnClickForAddCardButton(holder)

        setOnClickForDoneCardNameImageButton(holder, position)

        setOnClickForCloseCardImageButton(holder)
    }

    private fun setOnClickForTaskListTV(holder: ViewHolder) {
        val addTaskTV = holder.itemView.findViewById<TextView>(R.id.tv_add_task_list)
        addTaskTV.setOnClickListener {
            addTaskTV.visibility = View.GONE
            holder.itemView.findViewById<CardView>(R.id.cv_add_task_list_name).visibility =
                View.VISIBLE
        }
    }

    private fun setOnClickForCloseListImageButton(holder: ViewHolder) {
        val closeImageButton = holder.itemView.findViewById<ImageButton>(R.id.ib_close_list_name)
        closeImageButton.setOnClickListener {
            holder.itemView.findViewById<TextView>(R.id.tv_add_task_list).visibility = View.VISIBLE
            holder.itemView.findViewById<CardView>(R.id.cv_add_task_list_name).visibility =
                View.GONE
        }
    }

    private fun setOnClickForDoneListImageButton(holder: ViewHolder) {
        val doneListImageButton = holder.itemView.findViewById<ImageButton>(R.id.ib_done_list_name)
        doneListImageButton.setOnClickListener {
            val listName =
                holder.itemView.findViewById<TextView>(R.id.et_task_list_name).text.toString()
            if (listName.isNotEmpty()) {
                if (context is TaskListActivity) {
                    context.createTaskList(listName)
                }
            } else {
                Toast.makeText(
                    context, "Please Enter List Name",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun setOnClickForEditImageButton(holder: ViewHolder, model: Task) {
        val editButton = holder.itemView.findViewById<ImageButton>(R.id.ib_edit_list_name)
        editButton.setOnClickListener {
            holder.itemView.findViewById<EditText>(R.id.et_edit_task_list_name).setText(model.title)
            holder.itemView.findViewById<LinearLayout>(R.id.ll_title_view).visibility = View.GONE
            holder.itemView.findViewById<CardView>(R.id.cv_edit_task_list_name).visibility =
                View.VISIBLE
        }
    }

    private fun setOnClickForCloseEditableImageButton(holder: ViewHolder) {
        holder.itemView.findViewById<ImageButton>(R.id.ib_close_editable_view).setOnClickListener {
            holder.itemView.findViewById<LinearLayout>(R.id.ll_title_view).visibility = View.VISIBLE
            holder.itemView.findViewById<CardView>(R.id.cv_edit_task_list_name).visibility =
                View.GONE
        }
    }

    private fun setOnClickForDoneEditingImageButton(
        holder: ViewHolder,
        position: Int,
        model: Task
    ) {
        holder.itemView.findViewById<ImageButton>(R.id.ib_done_edit_list_name).setOnClickListener {
            val listName = holder.itemView
                .findViewById<EditText>(R.id.et_edit_task_list_name)
                .text.toString()
            if (listName.isNotEmpty()) {
                if (context is TaskListActivity) {
                    context.updateCardsInTaskList(position, list[position].cards)
                }
            } else {
                Toast.makeText(context, "Please Enter List Name", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setOnClickForDeleteImageButton(
        holder: ViewHolder,
        position: Int,
        modelTitle: String
    ) {
        holder.itemView.findViewById<ImageButton>(R.id.ib_delete_list).setOnClickListener {
            alertDialogForDeleteList(position, modelTitle)
        }
    }

    private fun alertDialogForDeleteList(position: Int, title: String) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Alert")
        builder.setMessage("Are you sure you want to delete $title.")
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton("Yes") { dialogInterface, _ ->
            dialogInterface.dismiss() // Dialog will be dismissed

            if (context is TaskListActivity) {
                context.deleteTaskList(position)
            }
        }

        builder.setNegativeButton("No") { dialogInterface, _ ->
            dialogInterface.dismiss() // Dialog will be dismissed
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false) // Will not allow user to cancel after clicking on remaining screen area.
        alertDialog.show()  // show the dialog to UI
    }

    private fun setOnClickForAddCardButton(holder: ViewHolder) {
        holder.itemView.findViewById<TextView>(R.id.tv_add_card).setOnClickListener {
            holder.itemView.findViewById<TextView>(R.id.tv_add_card).visibility = View.GONE
            holder.itemView.findViewById<CardView>(R.id.cv_add_card).visibility = View.VISIBLE
        }
    }

    private fun setOnClickForDoneCardNameImageButton(holder: ViewHolder, position: Int) {
        holder.itemView.findViewById<ImageButton>(R.id.ib_done_card_name).setOnClickListener {
            val cardName = holder.itemView.findViewById<EditText>(R.id.et_card_name).text.toString()
            if (cardName.isNotEmpty()) {
                if (context is TaskListActivity) {
                    context.addCardToTaskList(position, cardName)
                }
            } else {
                Toast.makeText(
                    context, "Please Enter A Card Name",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun setOnClickForCloseCardImageButton(holder: ViewHolder) {
        holder.itemView.findViewById<ImageButton>(R.id.ib_close_card_name).setOnClickListener {
            holder.itemView.findViewById<TextView>(R.id.tv_add_card).visibility = View.VISIBLE
            holder.itemView.findViewById<CardView>(R.id.cv_add_card).visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    private fun Int.toDP(): Int = (this / Resources.getSystem().displayMetrics.density).toInt()
    private fun Int.toPX(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

    class TaskListViewHolder(view: View) : RecyclerView.ViewHolder(view)
}
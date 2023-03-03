package com.example.projemanag.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projemanag.R
import com.example.projemanag.activities.TaskListActivity
import com.example.projemanag.model.Card
import com.example.projemanag.model.SelectedMember

open class CardListRecyclerAdapter(
    private val context: Context,
    private val list: ArrayList<Card>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return CardViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_card,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if (holder is CardViewHolder) {
            holder.itemView.findViewById<TextView>(R.id.tv_card_name).text = model.name
            val viewLabelColor: View = holder.itemView.findViewById<View>(R.id.view_label_color)

            if (model.labelColor.isNotEmpty()) {
                viewLabelColor.visibility = View.VISIBLE
                viewLabelColor.setBackgroundColor(
                    Color.parseColor(model.labelColor)
                )
            } else {
                viewLabelColor.visibility = View.GONE
            }

            val selectedMembersList: ArrayList<SelectedMember> =
                getSelectedMembersList(context as TaskListActivity, model.assignedTo)

            val isRecyclerViewVisible =
                setVisibilityOfRecyclerView(selectedMembersList, holder, model.createdBy)
            if (isRecyclerViewVisible) {
                setUpRecyclerViewForSelectedMembers(holder, selectedMembersList, position)
            }

            holder.itemView.setOnClickListener {
                if (onClickListener != null) {
                    onClickListener!!.onClick(position)
                }
            }
        }
    }

    private fun getSelectedMembersList(
        context: TaskListActivity,
        assignedTo: ArrayList<String>
    ): ArrayList<SelectedMember> {
        val selectedMembersList: ArrayList<SelectedMember> = ArrayList()
        if (context
                .assignedMemberDetailList.size > 0
        ) {
            for (i in context.assignedMemberDetailList.indices) {
                for (j in assignedTo) {
                    if (context.assignedMemberDetailList[i].uid == j) {
                        val selectedMember = SelectedMember(
                            context.assignedMemberDetailList[i].uid,
                            context.assignedMemberDetailList[i].image,
                        )
                        selectedMembersList.add(selectedMember)
                    }
                }
            }
        }
        return selectedMembersList
    }

    private fun setVisibilityOfRecyclerView(
        selectedMembersList: ArrayList<SelectedMember>,
        holder: CardViewHolder,
        createdBy: String
    ): Boolean {
        var isVisible = false
        val cardSelectedRecyclerView =
            holder.itemView.findViewById<RecyclerView>(R.id.rv_card_selected_members_list)

        if (selectedMembersList.size > 0) {
            if (selectedMembersList.size == 1 && selectedMembersList[0].uid == createdBy) {
                cardSelectedRecyclerView.visibility = View.GONE
            } else {
                cardSelectedRecyclerView.visibility = View.VISIBLE
                isVisible = true
            }
        } else {
            cardSelectedRecyclerView.visibility = View.GONE
        }
        return isVisible
    }

    private fun setUpRecyclerViewForSelectedMembers(
        holder: CardViewHolder,
        selectedMembersList: ArrayList<SelectedMember>,
        cardPosition: Int
    ) {
        val cardMemberRecyclerView =
            holder.itemView.findViewById<RecyclerView>(R.id.rv_card_selected_members_list)
        cardMemberRecyclerView.layoutManager = GridLayoutManager(context, 4)
        val adapter = CardMemberListRecyclerAdapter(context, selectedMembersList, false)
        cardMemberRecyclerView.adapter = adapter
        adapter.setOnClickListener(
            object : CardMemberListRecyclerAdapter.OnClickListener {
                override fun onClick() {
                    if (onClickListener != null) {
                        onClickListener!!.onClick(cardPosition)
                    }
                }
            }
        )

    }

    override fun getItemCount(): Int {
        return list.size
    }

    interface OnClickListener {
        fun onClick(cardPosition: Int)
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    class CardViewHolder(view: View) : RecyclerView.ViewHolder(view)

}

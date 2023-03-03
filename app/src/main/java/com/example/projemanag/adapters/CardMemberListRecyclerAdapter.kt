package com.example.projemanag.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.projemanag.R
import com.example.projemanag.model.SelectedMember
import de.hdodenhof.circleimageview.CircleImageView

open class CardMemberListRecyclerAdapter(
    private val context: Context,
    private var list: ArrayList<SelectedMember>,
    private val assignedMembers: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null
    private var selectedMemberList = this.list
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return CardMemberListViewHolder(LayoutInflater.from(context).inflate(
            R.layout.item_card_selected_member,
            parent,
            false
        ))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
       val model = selectedMemberList[position]
       if(holder is CardMemberListViewHolder){
           val addMemberImageView = holder.itemView.findViewById<ImageView>(R.id.iv_add_member)
           val selectedMemberImageView = holder.itemView.findViewById<ImageView>(R.id.iv_selected_member_image)
           if(position == selectedMemberList.size - 1 && assignedMembers){
               addMemberImageView.visibility = View.GONE
               selectedMemberImageView.visibility = View.GONE
           }else{
               addMemberImageView.visibility = View.GONE
               selectedMemberImageView.visibility = View.VISIBLE

               Glide
                   .with(context)
                   .load(model.image)
                   .placeholder(R.drawable.ic_user_place_holder)
                   .fitCenter()
                   .dontAnimate()
                   .into(holder.itemView.findViewById<CircleImageView>(R.id.iv_selected_member_image))
           }

           holder.itemView.setOnClickListener {
               if(onClickListener != null){
                   onClickListener!!.onClick()
               }
           }
       }
    }

    override fun getItemCount(): Int {
        return selectedMemberList.size
    }

    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener = onClickListener
    }

    fun updateList(list: ArrayList<SelectedMember>){
        this.selectedMemberList = list

    }

    interface OnClickListener{
        fun onClick()
    }

    class CardMemberListViewHolder(view: View) : RecyclerView.ViewHolder(view)
}

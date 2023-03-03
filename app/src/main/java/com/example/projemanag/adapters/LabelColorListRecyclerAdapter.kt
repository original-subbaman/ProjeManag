package com.example.projemanag.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.projemanag.R

class LabelColorListRecyclerAdapter(
    private val context: Context,
    private val colorList: ArrayList<String>,
    private val selectedColor: String)
 : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var onItemClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return LabelColorListViewHolder(
            LayoutInflater.from(context)
                .inflate(R.layout.item_label_color, parent, false))


    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = colorList[position]
        if(holder is LabelColorListViewHolder){
            holder.itemView.findViewById<View>(R.id.view_main).setBackgroundColor(
                Color.parseColor(item)
            )
            val checkMarkImageView : ImageView = holder.itemView.findViewById<ImageView>(R.id.iv_selected_color)
            if(item == selectedColor){
                checkMarkImageView.visibility = View.VISIBLE
            }else{
                checkMarkImageView.visibility = View.GONE
            }

            holder.itemView.setOnClickListener {
                if(onItemClickListener != null){
                    onItemClickListener!!.onClick(position, item)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return colorList.size
    }

    interface OnItemClickListener{
        fun onClick(position: Int, color: String)
    }

    private class LabelColorListViewHolder(view: View): RecyclerView.ViewHolder(view)
}
package com.example.projemanag.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projemanag.R
import com.example.projemanag.adapters.LabelColorListRecyclerAdapter

abstract class LabelColorListDialog(
    context: Context,
    private var list: ArrayList<String>,
    private var title: String = "",
    private var selectedColor: String = ""
) : Dialog(context) {

    private var adapter: LabelColorListRecyclerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = LayoutInflater.from(context).inflate( R.layout.dialog_list, null)
        setContentView(view)
        setCanceledOnTouchOutside(true)
        setCancelable(true)
        setUpRecyclerview(view)
    }

    private fun setUpRecyclerview(view: View) {
        view.findViewById<TextView>(R.id.tv_dialog_list_title).text = title
        val colorListRecyclerView: RecyclerView =
            view.findViewById<RecyclerView>(R.id.rv_dialog_list)
        colorListRecyclerView.layoutManager = LinearLayoutManager(context)
        adapter = LabelColorListRecyclerAdapter(
            context,
            list,
            selectedColor
        )
        colorListRecyclerView.adapter = this.adapter
        adapter!!.onItemClickListener =
            object : LabelColorListRecyclerAdapter.OnItemClickListener {
                override fun onClick(position: Int, color: String) {
                    dismiss()
                    onItemSelected(color)
                }
            }
    }

    protected abstract fun onItemSelected(color: String)


}
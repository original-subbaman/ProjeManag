package com.example.projemanag.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projemanag.R
import com.example.projemanag.adapters.MembersListRecyclerAdapter
import com.example.projemanag.model.User

abstract class MembersListDialog(
    context: Context,
    private val list: ArrayList<User>,
    private val title: String = "",
) : Dialog(context) {

    private var adapter: MembersListRecyclerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val view = LayoutInflater.from(context).inflate(
            R.layout.dialog_list, null
        )

        setContentView(view)
        setCanceledOnTouchOutside(true)
        setCancelable(true)
        setContentView(view)
        setUpRecyclerView(view)
    }

    private fun setUpRecyclerView(view: View) {
        view.findViewById<TextView>(R.id.tv_dialog_list_title).text = title
        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_dialog_list)
        if (list.size > 0) {
            recyclerView.layoutManager = LinearLayoutManager(context)
            adapter = MembersListRecyclerAdapter(context, list)
            recyclerView.adapter = adapter

            adapter!!.setOnClickListener(
                object : MembersListRecyclerAdapter.OnClickListener {
                    override fun onClick(position: Int, user: User, action: String) {
                        dismiss()
                        onItemSelected(user, action)
                    }
                }
            )
        }else{
            Log.e("this", "list size is 0")
        }
    }

    protected abstract fun onItemSelected(user: User, action: String)

}


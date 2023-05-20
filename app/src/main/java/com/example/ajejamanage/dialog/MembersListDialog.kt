package com.example.ajejamanage.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ajejamanage.R
import com.example.ajejamanage.adapters.MembersListItemsAdapter
import com.example.ajejamanage.models.User

abstract class MembersDialog(
    context:Context,
    private var list :ArrayList<User> ,
    private val title:String="",

    ):Dialog(context) {
    private var adapter:MembersListItemsAdapter?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view=LayoutInflater.from(context).inflate(R.layout.dialog_list,null)
        setContentView(view)
        setCanceledOnTouchOutside(true)
        setCancelable(true)
        setupRecycleView(view)
    }
    private fun setupRecycleView(view: View) {
        var ImageTitle: TextView = findViewById(R.id.tvTitle)
        var itemList: RecyclerView = findViewById(R.id.rvList)



        ImageTitle.text = title
        if(list.size>0){
        itemList.layoutManager = LinearLayoutManager(context)
        adapter = MembersListItemsAdapter(context, list)
        itemList.adapter = adapter
        adapter!!.setOnClickListener (object : MembersListItemsAdapter.OnClickListener{
            override fun onClick(position: Int, user: User, action:String) {
                dismiss()
                onItemSelected(user,action)

            }
        })

        }
    }



    protected abstract fun onItemSelected(user:User,action:String)

}
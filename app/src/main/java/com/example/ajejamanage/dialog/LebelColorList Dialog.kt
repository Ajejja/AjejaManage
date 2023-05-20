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
import com.example.ajejamanage.adapters.LabelColorListItemAdapter

abstract class `LabelColorListDialog`(
    context:Context,
    private var list :ArrayList<String> ,
    private val title:String="",
    private var mSelectedColor:String="",

):Dialog(context) {
    private var adapter:LabelColorListItemAdapter?=null
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
        itemList.layoutManager = LinearLayoutManager(context)
        adapter = LabelColorListItemAdapter(context, list, mSelectedColor)
        itemList.adapter = adapter
        adapter!!.onItemClickListener = object : LabelColorListItemAdapter.OnItemClickListener{
            override fun onClick(position: Int, color: String) {
                dismiss()
                onItemSelected(color)

            }

        }


    }
    protected abstract fun onItemSelected(color:String)

}
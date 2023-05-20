package com.example.ajejamanage.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ajejamanage.R
import com.example.ajejamanage.models.Board

class BoardItemsAdapter(
    private val context: Context,
    private var list: ArrayList<Board>,
    private var onClickListener: OnClickListener?=null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface OnClickListener {
        fun onClick(position: Int, model: Board)
    }
    fun setOnClickListener(onClickListener:OnClickListener){
        this.onClickListener=onClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(R.layout.item_board, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]
        if (holder is MyViewHolder) {
            val iv_boardImage: ImageView = holder.itemView.findViewById(R.id.iv_boardImage)
            val tvName: TextView = holder.itemView.findViewById(R.id.tv_name)
            val tvCreatedBy: TextView = holder.itemView.findViewById(R.id.tv_created_by)
            Glide.with(context)
                .load(model.image)
                .centerCrop()
                .placeholder(R.drawable.baseline_circle_24)
                .into(iv_boardImage)
            tvName.text = model.name
            tvCreatedBy.text = "Created by: ${model.createdBy}"
            holder.itemView.setOnClickListener {
                onClickListener!!.onClick(position, model)
            }
        }
    }

    private class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}

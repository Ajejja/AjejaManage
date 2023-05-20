package com.example.ajejamanage.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ajejamanage.R
import com.example.ajejamanage.models.SelectedMembers

open class CardMembersIListAdapter (
    private val context: Context,
    private val list:ArrayList<SelectedMembers>,
    private val assignedMember:Boolean
        ):RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    private var onClickListener: CardMembersIListAdapter.OnClickListener?=null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
      return MyViewHolder(LayoutInflater.from(context).inflate(
          R.layout.item_card_selected_member,parent,false
      )
      )
    }

    override fun getItemCount(): Int {
        return list.size
    }
    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener=onClickListener

    }
    interface OnClickListener{
        fun OnClick()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model=list[position]
        if(holder is MyViewHolder){
            if(position ==list.size-1&&assignedMember){
                val addMembers: View = holder.itemView.findViewById(R.id.iv_add_member)
                val selectedMembersImage: View = holder.itemView.findViewById(R.id.iv_selected_member_image)

                addMembers.visibility=View.VISIBLE
                selectedMembersImage.visibility=View.GONE
            }else{
                val addMembers: ImageView = holder.itemView.findViewById(R.id.iv_add_member)
                val selectedMembersImage: ImageView = holder.itemView.findViewById(R.id.iv_selected_member_image)

                addMembers.visibility=View.GONE
                selectedMembersImage.visibility=View.VISIBLE
                Glide
                    .with(context)
                    .load(model.image)
                    .centerCrop()
                    .placeholder(R.drawable.ic_user_place_holder)
                    .into(selectedMembersImage)

            }
            holder.itemView.setOnClickListener {
                if(onClickListener!=null){
                    onClickListener!!.OnClick()
                }
            }
        }
    }
    class MyViewHolder(view: View):RecyclerView.ViewHolder(view)
}
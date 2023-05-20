package com.example.ajejamanage.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ajejamanage.R
import com.example.ajejamanage.activities.TaskActivity
import com.example.ajejamanage.models.Card
import com.example.ajejamanage.models.SelectedMembers

open class CardListItemsAdapter(private val context: Context,
private var list:ArrayList<Card>):RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var onClickListener:OnClickListener?=null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return TaskListItemAdapter.MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_card,
                parent,false
            )
        )
    }
    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener=onClickListener

    }
    interface OnClickListener{
        fun OnClick(position: Int)
    }
    class MyViewHolder(view:View):RecyclerView.ViewHolder(view)

    override fun getItemCount(): Int {
        return list.size
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model=list[position]
        if (holder is TaskListItemAdapter.MyViewHolder){
            if(model.labelColor.isNotEmpty()){
                val labelColor: View = holder.itemView.findViewById(R.id.view_lavel_color)
                labelColor.visibility=View.VISIBLE
                labelColor.setBackgroundColor(Color.parseColor(model.labelColor))

            }else{
                val labelColor: View = holder.itemView.findViewById(R.id.view_lavel_color)
                labelColor.visibility=View.GONE

            }
            var CardName: TextView =holder.itemView.findViewById(R.id.tv_card_name)
            CardName.text=model.name
            if((context  as TaskActivity).mAssignedMembersDetailList.size>0){
                val selectedMembersList:ArrayList<SelectedMembers> = ArrayList()
                for(i in context.mAssignedMembersDetailList.indices){
                    for(j in model.assignedTo){
                        if(context.mAssignedMembersDetailList[i].id==j){
                            val selectedMembers=SelectedMembers(context.mAssignedMembersDetailList[i].id,
                                context.mAssignedMembersDetailList[i].image)
                            selectedMembersList.add(selectedMembers)
                        }
                    }
                }
                if(selectedMembersList.size>0){
                    if(selectedMembersList.size==1&&selectedMembersList[0].id==model.createdBy){
                        var cardSelectedMembersList: RecyclerView =holder.itemView.findViewById(R.id.rv_card_selected_members_list)

                        cardSelectedMembersList.visibility=View.GONE
                    }else{
                        var cardSelectedMembersList: RecyclerView =holder.itemView.findViewById(R.id.rv_card_selected_members_list)

                        cardSelectedMembersList.visibility=View.VISIBLE

                        cardSelectedMembersList.layoutManager=GridLayoutManager(context,4)
                        val adapter=CardMembersIListAdapter(context,selectedMembersList,false)

                        cardSelectedMembersList.adapter=adapter
                        adapter.setOnClickListener(object :CardMembersIListAdapter.OnClickListener{
                            override fun OnClick(){
                                if(onClickListener!=null){
                                    onClickListener!!.OnClick(position)
                                }

                            }

                        })
                    }
                }else{
                    var cardSelectedMembersList: RecyclerView =holder.itemView.findViewById(R.id.rv_card_selected_members_list)
                    cardSelectedMembersList.visibility=View.GONE

                }
            }
            holder.itemView.setOnClickListener {
                if(onClickListener!=null){
                    onClickListener!!.OnClick(position)
                }
            }

        }
    }
}
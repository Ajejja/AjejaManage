package com.example.ajejamanage.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ajejamanage.R
import com.example.ajejamanage.activities.TaskActivity
import com.example.ajejamanage.models.Task
import java.util.Collections

open class TaskListItemAdapter(private val context: Context,
                               private var list:ArrayList<Task>):RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var mPositionDraggedFrom=-1
    private var mPositionDraggedTo=-1


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val view=LayoutInflater.from(context).inflate(R.layout.item_task,parent,false)
        val layoutParams=LinearLayout.LayoutParams(
            (parent.width * 0.75).toInt(),LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(
            (15.toDp().toPx()),0,(40.toDp()).toPx(),0)
        view.layoutParams=layoutParams
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
             return list.size
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val model=list[position]
        if (holder is MyViewHolder){
            if(position==list.size-1){
                var tvAddList:TextView = holder.itemView.findViewById(R.id.tv_add_task_list)
                tvAddList.visibility=View.VISIBLE
                var taskItem:LinearLayout=holder.itemView.findViewById(R.id.ll_task_item)
                taskItem.visibility=View.GONE


            }else{
                var tvAddList:TextView = holder.itemView.findViewById(R.id.tv_add_task_list)
                tvAddList.visibility=View.GONE
                var taskItem:LinearLayout=holder.itemView.findViewById(R.id.ll_task_item)
                taskItem.visibility=View.VISIBLE
            }
            var taskListTitle:TextView=holder.itemView.findViewById(R.id.tv_task_list_title)
            taskListTitle.text=model.title
            var AddTaskList:TextView=holder.itemView.findViewById(R.id.tv_add_task_list)
            AddTaskList.setOnClickListener {
                var tvAddList:TextView = holder.itemView.findViewById(R.id.tv_add_task_list)
                tvAddList.visibility=View.GONE
                var addTaskListName:CardView=holder.itemView.findViewById(R.id.cv_add_task_list_name)
                addTaskListName.visibility=View.VISIBLE
            }
            var CloseListNAme:ImageView=holder.itemView.findViewById(R.id.ib_close_list_name)
            CloseListNAme.setOnClickListener {
                var tvAddList:TextView = holder.itemView.findViewById(R.id.tv_add_task_list)
                tvAddList.visibility=View.VISIBLE
                var addTaskListName:CardView=holder.itemView.findViewById(R.id.cv_add_task_list_name)
                addTaskListName.visibility=View.GONE
            }
            var doneListNAme:ImageView=holder.itemView.findViewById(R.id.ib_done_list_name)
            doneListNAme.setOnClickListener {
                val et_task_list_name:EditText=holder.itemView.findViewById<EditText?>(R.id.et_task_list_name)
               val listName= et_task_list_name.text.toString()
                if(listName.isNotEmpty()){
                    if(context is TaskActivity){
                        context.createTaskList(listName)
                    }
                }else{
                    Toast.makeText(context,"Please Enter a List Name.",Toast.LENGTH_SHORT).show()
                }

            }

        }
        var EditListNAme:ImageView=holder.itemView.findViewById(R.id.ib_edit_list_name)
        EditListNAme.setOnClickListener {
            val et_edit_task_list_name:EditText=holder.itemView.findViewById<EditText?>(R.id.et_edit_task_list_name)
            var titleView:LinearLayout=holder.itemView.findViewById(R.id.ll_title_view)
            var EditTaskListNAme:CardView=holder.itemView.findViewById(R.id.cv_edit_task_list_name)
            et_edit_task_list_name.setText(model.title)
            titleView.visibility=View.GONE
            EditTaskListNAme.visibility=View.VISIBLE


        }
        var CloseeditableView:ImageView=holder.itemView.findViewById(R.id.ib_close_editable_view)
        CloseeditableView.setOnClickListener {
            var titleView:LinearLayout = holder.itemView.findViewById(R.id.ll_title_view)
            titleView.visibility=View.VISIBLE
            var editTask:CardView=holder.itemView.findViewById(R.id.cv_edit_task_list_name)
            editTask.visibility=View.GONE
        }
        var doneEditListNAme: ImageView = holder.itemView.findViewById(R.id.ib_done_edit_list_name)
        doneEditListNAme.setOnClickListener {
            val et_task_list_name: EditText = holder.itemView.findViewById<EditText?>(R.id.et_edit_task_list_name)
            val listName = et_task_list_name.text.toString()
            if (listName.isNotEmpty()) {
                if (context is TaskActivity) {
                    context.updateTaskList(position, listName, model)
                }
            } else {
                Toast.makeText(context, "Please Enter a List Name.", Toast.LENGTH_SHORT).show()
            }
        }
        var DeleteListNAme:ImageView=holder.itemView.findViewById(R.id.ib_delete_list)
        DeleteListNAme.setOnClickListener {
            alertDialogForDeleteList(position, model.title)
        }
        var AddCArd:TextView=holder.itemView.findViewById(R.id.tv_add_card)
        AddCArd.setOnClickListener {
            AddCArd.visibility=View.GONE
            var AddCArdView:CardView=holder.itemView.findViewById(R.id.cv_add_card)
            AddCArdView.visibility=View.VISIBLE

        }
        var CloseCardName: ImageView = holder.itemView.findViewById(R.id.ib_close_card_name)
        CloseCardName.setOnClickListener {
            AddCArd.visibility=View.VISIBLE
            var AddCArdView:CardView=holder.itemView.findViewById(R.id.cv_add_card)
            AddCArdView.visibility=View.GONE
        }
        var doneCardNAme:ImageView=holder.itemView.findViewById(R.id.ib_done_card_name)
        doneCardNAme.setOnClickListener {
            val et_task_list_name:EditText=holder.itemView.findViewById<EditText?>(R.id.et_card_name)
            val CardName= et_task_list_name.text.toString()
            if(CardName.isNotEmpty()){
                if(context is TaskActivity){
                    context.addCardToTaskList(position, CardName )          }
            }else{
                Toast.makeText(context,"Please Enter a card Name.",Toast.LENGTH_SHORT).show()
            }

        }
        val CardList:RecyclerView=holder.itemView.findViewById(R.id.rv_card_list)

        CardList.layoutManager=LinearLayoutManager(context)
        CardList.setHasFixedSize(true)
        val adapter=CardListItemsAdapter(context,model.cards)
        CardList.adapter=adapter
        adapter.setOnClickListener(

            object :CardListItemsAdapter.OnClickListener{
                override fun OnClick(CardPosition: Int) {
                    if(context is TaskActivity){
                        context.CardDetails(position,CardPosition)
                    }
                }
            }
        )
        val dividerItemDecoration=DividerItemDecoration(context,
            DividerItemDecoration.VERTICAL)
        var cardListRecyclerView: RecyclerView =holder.itemView.findViewById(R.id.rv_card_list)
        cardListRecyclerView.addItemDecoration(dividerItemDecoration)
        val helper=ItemTouchHelper(
            object:ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP or ItemTouchHelper.DOWN,0
            ){
                override fun onMove(
                    recyclerView: RecyclerView,
                    dragged: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    val draggedPosition=dragged.adapterPosition
                    val targetPosition=target.adapterPosition
                    if(mPositionDraggedFrom==-1){
                        mPositionDraggedFrom=draggedPosition
                    }
                    mPositionDraggedTo=targetPosition
                    Collections.swap(list[position].cards,draggedPosition,targetPosition)
                    adapter.notifyItemMoved(draggedPosition,targetPosition)
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                }

                override fun clearView(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder
                ) {
                    super.clearView(recyclerView, viewHolder)
                    if(mPositionDraggedFrom!=-1&&mPositionDraggedTo!=-1
                        &&mPositionDraggedFrom!=mPositionDraggedTo){
                        (context as TaskActivity).updateCardsInTaskList(position,
                            list[position].cards)
                    }
                    mPositionDraggedFrom=-1
                    mPositionDraggedTo=-1
                }

            }
        )

        helper.attachToRecyclerView(cardListRecyclerView)








    }
    private fun alertDialogForDeleteList(position: Int,title:String) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Alert")
        builder.setIcon(R.drawable.alert)
        builder.setMessage("Are you sure you want to delete $title")
        builder.setPositiveButton("yes") { dialogInterface, which ->
            dialogInterface.dismiss()
            if (context is TaskActivity) {
                context.deleteTaskList(position)
            }
        }
        builder.setNegativeButton("No") { dialogInterface, which ->
            dialogInterface.dismiss()
        }
        val alertDialog:AlertDialog=builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }
    private fun Int.toDp():Int=(this/Resources.getSystem().displayMetrics.density).toInt()
    private fun Int.toPx():Int=(this*Resources.getSystem().displayMetrics.density).toInt()
    class MyViewHolder(view:View):RecyclerView.ViewHolder(view)

}
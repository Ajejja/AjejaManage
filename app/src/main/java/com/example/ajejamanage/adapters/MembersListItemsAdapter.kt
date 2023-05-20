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
import com.example.ajejamanage.models.User
import com.example.ajejamanage.utils.Constants
import de.hdodenhof.circleimageview.CircleImageView

open class MembersListItemsAdapter(
    private val context: Context,
    private var list: ArrayList<User>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(R.layout.item_member, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]
        val memberImage: CircleImageView = holder.itemView.findViewById(R.id.iv_member_image)
        val memberName: TextView = holder.itemView.findViewById(R.id.tv_member_name)
        val memberEmail: TextView = holder.itemView.findViewById(R.id.tv_member_email)
        if (holder is MyViewHolder) {
            Glide.with(context)
                .load(model.image)
                .centerCrop()
                .placeholder(R.drawable.ic_user_place_holder)
                .into(memberImage)

            memberName.text = model.name
            memberEmail.text = model.email
            if (model.selected) {
                val doneImage: ImageView = holder.itemView.findViewById(R.id.iv_selected_member)
                doneImage.visibility = View.VISIBLE
            } else {
                val doneImage: ImageView = holder.itemView.findViewById(R.id.iv_selected_member)
                doneImage.visibility = View.GONE
            }
            holder.itemView.setOnClickListener {

                if (onClickListener != null) {
                    // TODO (Step 3: Pass the constants here according to the selection.)
                    // START
                    if (model.selected) {
                        onClickListener!!.onClick(position, model, Constants.UN_SELECT)
                    } else {
                        onClickListener!!.onClick(position, model, Constants.SELECT)
                    }
                    // END
                }
            }

        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick(position: Int, user: User, action: String)
    }



    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}




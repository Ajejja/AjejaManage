package com.example.ajejamanage.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.icu.text.SimpleDateFormat
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ajejamanage.R
import com.example.ajejamanage.adapters.CardMembersIListAdapter
import com.example.ajejamanage.adapters.LabelColorListItemAdapter
import com.example.ajejamanage.adapters.MembersListItemsAdapter
import com.example.ajejamanage.dialog.LabelColorListDialog
import com.example.ajejamanage.dialog.MembersDialog
import com.example.ajejamanage.firebase.firestoreClass
import com.example.ajejamanage.models.Board
import com.example.ajejamanage.models.Card
import com.example.ajejamanage.models.SelectedMembers
import com.example.ajejamanage.models.Task
import com.example.ajejamanage.models.User
import com.example.ajejamanage.utils.Constants
import com.google.android.material.button.MaterialButton
import java.util.Calendar
import java.util.Date
import java.util.Locale


class CardDetailsActivity : BaseActivity() {
    private val toolbarCArdDetails: Toolbar by lazy { findViewById<Toolbar>(R.id.toolbar_card_details_activity) }
    private lateinit var mBoardDetails:Board
    private lateinit var mMembersDetailList:ArrayList<User>
    private var mTaskListPosition=-1
    private var mCardPosition=-1
    private var mselectedColor=""
    private val NameCardDetail: EditText by lazy { findViewById<EditText>(R.id.et_name_card_details) }
    private val btnUpdate: MaterialButton by lazy { findViewById<MaterialButton>(R.id.btn_update_card_details) }
    private val selecLabelColor: TextView by lazy { findViewById<TextView>(R.id.tv_select_label_color) }
    private val selectMembers: TextView by lazy { findViewById<TextView>(R.id.tv_select_members) }
    private val rvselectMembersList: RecyclerView by lazy { findViewById<RecyclerView>(R.id.re_selected_members_List) }
    private var mSelectedDueDateMilliSeconds:Long=0
    private val selectedDateTextView: TextView by lazy { findViewById<TextView>(R.id.tv_select_due_date) }






    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_details)
        getIntentData()
        setupActionBar()
        NameCardDetail.setText(mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].name)
        NameCardDetail.setSelection(NameCardDetail.text.toString().length)
        mselectedColor=mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].labelColor
        if(mselectedColor.isNotEmpty()){
            setColor()

        }
        btnUpdate.setOnClickListener {
            if(NameCardDetail.text.toString().isNotEmpty()){
                updateCardDetails()

            }
            else{
                Toast.makeText(this@CardDetailsActivity,"Enter a card name",Toast.LENGTH_SHORT).show()
            }
        }
        selecLabelColor.setOnClickListener {
            labelColorsListDialog()
        }
        selectMembers.setOnClickListener {
            membersListDialog()
        }
        setupSelectedMembersList()
        mSelectedDueDateMilliSeconds=mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].dueDate
        if(mSelectedDueDateMilliSeconds>0){
            val simpleDateFormat=java.text.SimpleDateFormat("dd/MM/yyyy",Locale.ENGLISH)
            val selectedDate = simpleDateFormat.format(Date(mSelectedDueDateMilliSeconds))
            selectedDateTextView.text=selectedDate
        }
        selectedDateTextView.setOnClickListener {
            showDataPicker()
        }

    }
    private fun setupActionBar() {
        setSupportActionBar(toolbarCArdDetails)
        var actionBar=supportActionBar
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_ios_23)
            actionBar.title=mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].name


        }
        toolbarCArdDetails.setNavigationOnClickListener {
            onBackPressed()
            updateCardDetails()
        }

    }
    private fun colorsList():ArrayList<String>{
        val colorList:ArrayList<String> = ArrayList()
        colorList.add("#43C86F")
        colorList.add("#0C90F1")
        colorList.add("#F72400")
        colorList.add("#7A8089")
        colorList.add("#D57C1D")
        colorList.add("#770000")
        colorList.add("#0022F8")
        return colorList

    }
    private fun setColor(){
        selecLabelColor.text=""
        selecLabelColor.setBackgroundColor(Color.parseColor(mselectedColor))
    }
    private fun labelColorsListDialog()
    {
        val colorsList:ArrayList<String> =colorsList()
        val listDialog=object :LabelColorListDialog(this,colorsList,"Select label color",mselectedColor){
            override fun onItemSelected(color: String) {
                mselectedColor=color
                setColor()


            }


        }
        listDialog.show()

    }
    private fun membersListDialog(){
        var cardAssignedMembersList=mBoardDetails.taskList[mTaskListPosition]
            .cards[mCardPosition].assignedTo
        if (cardAssignedMembersList.size>0){
            for(i in mMembersDetailList.indices){
                for(j in cardAssignedMembersList){
                    if(mMembersDetailList[i].id==j){
                        mMembersDetailList[i].selected=true
                    }
                }
            }
        }else{
            for(i in mMembersDetailList.indices){

                    mMembersDetailList[i].selected=false

        }

        }
        val listDialog=object: MembersDialog(
            this ,mMembersDetailList,"Select Member"
        ){
            override fun onItemSelected(user: User, action: String) {
                if(action ==Constants.SELECT){
                    if(!mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo
                            .contains(user.id)){
                        mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo.add(user.id)

                    }}else{
                        mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo.remove(user.id)
                        for(i in mMembersDetailList.indices){
                            if(mMembersDetailList[i].id==user.id){
                                mMembersDetailList[i].selected=false
                            }
                        }
                    }
                    setupSelectedMembersList()

                }
            }


            listDialog.show()
    }
    private fun updateCardDetails() {
        val card = Card(
            NameCardDetail.text.toString(),
            mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].createdBy,
            mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo,
            mselectedColor,mSelectedDueDateMilliSeconds
        )
        val taslList:ArrayList<Task> =mBoardDetails.taskList
        taslList.removeAt(taslList.size-1)
        mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition] = card
        showProgressDialog("Please wait")

        // Pass the updated board details back to the TaskActivity
        val resultIntent = Intent()
        resultIntent.putExtra(Constants.BOARD_DETAIL, mBoardDetails)
        resultIntent.putExtra(Constants.TASK_LIST_ITEM_POSITION, mTaskListPosition)
        resultIntent.putExtra(Constants.CARD_LIST_ITEM_POSITION, mCardPosition)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()

    }
    private fun setupSelectedMembersList(){
        val cardAssignedMembersList=mBoardDetails.taskList[mTaskListPosition]
            .cards[mCardPosition].assignedTo
        val selectedMembersList:ArrayList<SelectedMembers> =ArrayList()
            for(i in mMembersDetailList.indices){
                for(j in cardAssignedMembersList){
                    if(mMembersDetailList[i].id==j){
                        val selectedMember=SelectedMembers(mMembersDetailList[i].id,mMembersDetailList[i].image)
                        selectedMembersList.add(selectedMember)
                    }
                }
            }
        if (selectedMembersList.size>0){
            selectedMembersList.add(SelectedMembers("",""))
            selectMembers.visibility=View.GONE
            rvselectMembersList.visibility=View.VISIBLE
            rvselectMembersList.layoutManager=GridLayoutManager(
                this,6
            )
            val adapter=CardMembersIListAdapter(this,selectedMembersList,true)
            rvselectMembersList.adapter=adapter
            adapter.setOnClickListener(
                object :CardMembersIListAdapter.OnClickListener{
                    override fun OnClick(){
                        membersListDialog()
                    }
                }
            )
        }else{
            selectMembers.visibility=View.VISIBLE
            rvselectMembersList.visibility=View.GONE
        }



    }


     fun addupdateTaskListSuccess(){
        hideProgressDialog()

        setResult(Activity.RESULT_OK)
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_delete_card,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_delete_card->{
                alertDialogForDeleteList( mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].name)
                return true}


        }
        return super.onOptionsItemSelected(item)
    }
    private fun getIntentData(){
        if(intent.hasExtra(Constants.BOARD_DETAIL)){
            mBoardDetails=intent.getParcelableExtra(Constants.BOARD_DETAIL)!!

        }
        if(intent.hasExtra(Constants.TASK_LIST_ITEM_POSITION)){
            mTaskListPosition=intent.getIntExtra(Constants.TASK_LIST_ITEM_POSITION,-1)

        }
        if(intent.hasExtra(Constants.CARD_LIST_ITEM_POSITION)){
            mCardPosition=intent.getIntExtra(Constants.CARD_LIST_ITEM_POSITION,-1)

        }
        if(intent.hasExtra(Constants.BOARD_MEMBERS_LIST)){
            mMembersDetailList=intent.getParcelableArrayListExtra(Constants.BOARD_MEMBERS_LIST)!!

        }
    }
    private fun deleteCard(){
        val cardsList:ArrayList<Card> =mBoardDetails.taskList[mTaskListPosition].cards
        cardsList.removeAt(mCardPosition)
        val taskList:ArrayList<Task> =mBoardDetails.taskList
        taskList.removeAt(taskList.size-1)
        taskList[mTaskListPosition].cards=cardsList
        showProgressDialog("Please wait")
        firestoreClass().addUpdateTaskList(this@CardDetailsActivity,mBoardDetails)
        finish()

    }
    private fun alertDialogForDeleteList(cardName:String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Alert")
        builder.setIcon(R.drawable.alert)
        builder.setMessage("Are you sure you want to delete $cardName")
        builder.setPositiveButton("yes") { dialogInterface, which ->
            dialogInterface.dismiss()
            deleteCard()
        }
        builder.setNegativeButton("No") { dialogInterface, which ->
            dialogInterface.dismiss()
        }
        val alertDialog: AlertDialog =builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }
    private fun showDataPicker(){
        val c=Calendar.getInstance()
        val year=c.get(Calendar.YEAR)
        val month=c.get(Calendar.MONTH)
        val day=c.get(Calendar.DAY_OF_MONTH)
        val dpd=DatePickerDialog(
            this,DatePickerDialog.OnDateSetListener{
                view,year,monthOfYear,dayOfMonth->
                val sDayOfMonth=
                    if(dayOfMonth<10){"0$dayOfMonth"}
                    else{"${dayOfMonth}"}
                val sMonthOfYear=
                    if((monthOfYear)<10){"0${monthOfYear+1}"}
                    else{{"$monthOfYear+1"}}
                val selectedDate="$sDayOfMonth/$sMonthOfYear/$year"
                selectedDateTextView.text=selectedDate
                val sdf=java.text.SimpleDateFormat("dd/MM/yyyy",Locale.ENGLISH)
                val theDate=sdf.parse(selectedDate)
                mSelectedDueDateMilliSeconds=theDate!!.time

            },year,month,day
        )
        dpd.show()
    }


}

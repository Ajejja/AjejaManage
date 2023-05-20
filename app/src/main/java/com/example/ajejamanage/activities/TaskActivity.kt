package com.example.ajejamanage.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ajejamanage.R
import com.example.ajejamanage.activities.MainActivity.Companion.TASK_ACTIVITY_REQUEST_CODE
import com.example.ajejamanage.adapters.TaskListItemAdapter
import com.example.ajejamanage.firebase.firestoreClass
import com.example.ajejamanage.models.Board
import com.example.ajejamanage.models.Card
import com.example.ajejamanage.models.Task
import com.example.ajejamanage.models.User
import com.example.ajejamanage.utils.Constants

class TaskActivity : BaseActivity() {

    private lateinit var adapter: TaskListItemAdapter
    private lateinit var mBoardDetails:Board
    lateinit var mAssignedMembersDetailList:ArrayList<User>
    private val taskList: RecyclerView by lazy { findViewById<RecyclerView>(R.id.rv_task_list) }
    private val toolbarTask: Toolbar by lazy { findViewById<Toolbar>(R.id.toolbarTaskActivity) }
    private lateinit var mBoardDocumentId:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task)
        if(intent.hasExtra(Constants.DOCUMENT_ID)){
            mBoardDocumentId=intent.getStringExtra(Constants.DOCUMENT_ID)!!
        }
        showProgressDialog("Please Wait")
        firestoreClass().getBoardDetails(this,mBoardDocumentId)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ( resultCode == Activity.RESULT_OK ){
            if (data != null) {
                val updatedBoard = data.getParcelableExtra<Board>(Constants.BOARD_DETAIL)
                if (updatedBoard != null) {
                    mBoardDetails = updatedBoard
                    adapter.notifyDataSetChanged()
                    showProgressDialog("Please wait")
                    firestoreClass().addUpdateTaskList(this, mBoardDetails)
                }
            }
        }
    }
    fun updateCardsInTaskList(taskListPosition:Int,cards:ArrayList<Card>){
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size-1)
        mBoardDetails.taskList[taskListPosition].cards=cards
        showProgressDialog("Please Wait")
        firestoreClass().addUpdateTaskList(this@TaskActivity, mBoardDetails)

    }






    fun boardDetails(board:Board){
        mBoardDetails=board
        hideProgressDialog()
        setupActionBar()

        showProgressDialog("Please wait")
        firestoreClass().getAssignedMembersListDetails(this,mBoardDetails.assignedTo)



    }
    fun boardMembersDetailsList(list:ArrayList<User>){
        mAssignedMembersDetailList=list
        hideProgressDialog()
        val addTaskList=Task("Add List")
        mBoardDetails.taskList.add(addTaskList)
        taskList.layoutManager=LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
        taskList.setHasFixedSize(true)
        val adapter=TaskListItemAdapter(this ,mBoardDetails.taskList)
        taskList.adapter=adapter
        this.adapter = TaskListItemAdapter(this, mBoardDetails.taskList)
        taskList.adapter = adapter

    }
    fun createTaskList(taskListName:String){
        val task=Task(taskListName,firestoreClass().getCurrentUserId())
        mBoardDetails.taskList.add(0,task)
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size-1)
        showProgressDialog("Please wait")
        firestoreClass().addUpdateTaskList(this,mBoardDetails)

    }
    fun updateTaskList(position: Int, listName: String, model: Task) {
        val updatedTask = Task(listName, model.createdBy, model.cards) // Use the existing cards list
        mBoardDetails.taskList[position] = updatedTask
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)
        showProgressDialog("Please wait")
        firestoreClass().addUpdateTaskList(this, mBoardDetails)
    }

    fun deleteTaskList(position: Int){
        mBoardDetails.taskList.removeAt(position)
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)
        showProgressDialog("please wait")
        firestoreClass().addUpdateTaskList(this,mBoardDetails)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_members,menu)
        menuInflater.inflate(R.menu.menu_delete_card,menu)
        return super.onCreateOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_members -> {
                val intent = Intent(this, MembersActivity::class.java)
                intent.putExtra(Constants.BOARD_DETAIL, mBoardDetails)
                startActivityForResult(intent, MEMBER_REQUEST_CODE)
                return true
            }
            R.id.action_delete_card -> {
                deleteBoard()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }




    private fun deleteBoard() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Board")
        builder.setMessage("Are you sure you want to delete this board?")
        builder.setPositiveButton("Delete") { _, _ ->
            showProgressDialog("Deleting Board...")
            firestoreClass().deleteBoard(this, mBoardDetails.documentId)
            firestoreClass().getBoardsList(MainActivity())

        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        val alertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()

    }

    fun boardDeletedSuccessfully() {

        hideProgressDialog()
        Toast.makeText(this@TaskActivity, "Board deleted successfully", Toast.LENGTH_SHORT).show()
        val intent = Intent()
        intent.putExtra("boardDeleted", true)
        setResult(Activity.RESULT_OK, intent)
        finish()


    }













    fun addUpdateTaskListSuccess(){
        hideProgressDialog()
        showProgressDialog("Please Wait")
        firestoreClass().getBoardDetails(this,mBoardDetails.documentId)
    }
    fun addCardToTaskList(position: Int,cardName:String){
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)
        val cardAssignedUserList:ArrayList<String> = ArrayList()
        cardAssignedUserList.add(firestoreClass().getCurrentUserId())
        val card= Card(cardName,firestoreClass().getCurrentUserId(),cardAssignedUserList)
        val cardList=mBoardDetails.taskList[position].cards
        cardList.add(card)
        val task=Task(
            mBoardDetails.taskList[position].title,
            mBoardDetails.taskList[position].createdBy,
            cardList
        )
        mBoardDetails.taskList[position]=task
        showProgressDialog("please wait")
        firestoreClass().addUpdateTaskList(this,mBoardDetails)

    }
    fun CardDetails(taskListPosition:Int,cardPosition:Int){
        val intent=Intent(this,CardDetailsActivity::class.java)
        intent.putExtra(Constants.BOARD_DETAIL,mBoardDetails)
        intent.putExtra(Constants.TASK_LIST_ITEM_POSITION,taskListPosition)
        intent.putExtra(Constants.CARD_LIST_ITEM_POSITION,cardPosition)
        intent.putExtra(Constants.BOARD_MEMBERS_LIST,mAssignedMembersDetailList)
        startActivityForResult(intent, CARD_DETAILS_REQUEST_CODE)
    }
    private fun setupActionBar() {
        setSupportActionBar(toolbarTask)
        var actionBar=supportActionBar
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_ios_23)
            actionBar.title=mBoardDetails.name

        }
        toolbarTask.setNavigationOnClickListener {
            onBackPressed()
        }

    }
    companion object{
        const val MEMBER_REQUEST_CODE:Int=13
        const val CARD_DETAILS_REQUEST_CODE:Int=14
        const val BOARD_REQUEST_CODE:Int=15


    }
}
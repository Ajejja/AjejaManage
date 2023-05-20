package com.example.ajejamanage.activities

import android.app.Activity
import android.app.Dialog
import android.os.AsyncTask
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ajejamanage.R
import com.example.ajejamanage.adapters.MembersListItemsAdapter
import com.example.ajejamanage.firebase.firestoreClass
import com.example.ajejamanage.models.Board
import com.example.ajejamanage.models.User
import com.example.ajejamanage.utils.Constants
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutput
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.io.StringBufferInputStream
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

class MembersActivity : BaseActivity() {
    private val MembersList: RecyclerView by lazy { findViewById(R.id.rv_members_list) }
    private lateinit var mBoardDetails:Board
    private lateinit var mAssignedMembersList:ArrayList<User>
    private val toolbarMembersActivity: Toolbar by lazy { findViewById<Toolbar>(R.id.toolbar_members_activity) }
     private var anyChangeMode:Boolean=false




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_members)
        if (intent.hasExtra(Constants.BOARD_DETAIL)) {
            mBoardDetails = intent.getParcelableExtra<Board>(Constants.BOARD_DETAIL)!!
            showProgressDialog("Please Wait")
        }
            // Fetch assigned members for each user ID in the `assignedTo` list

                firestoreClass().getAssignedMembersListDetails(
                    this@MembersActivity,
                    mBoardDetails.assignedTo
                )

        setupActionBar()
    }

    fun memberDetails(user:User){
        mBoardDetails.assignedTo.add(user.id)
        firestoreClass().assignMemebersToBoard(this@MembersActivity,mBoardDetails,user)

    }

    fun setupMembersList(list:ArrayList<User>){
        mAssignedMembersList=list
        hideProgressDialog()
        MembersList.layoutManager=LinearLayoutManager(this@MembersActivity)
        MembersList.setHasFixedSize(true)
        val adapter=MembersListItemsAdapter(this@MembersActivity,list)
        MembersList.adapter=adapter

    }
    private fun setupActionBar() {
        setSupportActionBar(toolbarMembersActivity)
        var actionBar=supportActionBar
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_ios_23)
            actionBar.title="Members"

        }
        toolbarMembersActivity.setNavigationOnClickListener {
            onBackPressed()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
      menuInflater.inflate(R.menu.menu_add_member,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.action_add_member->{
                dialogSearchMember()
                return true

            }
        }
        return super.onOptionsItemSelected(item)
    }
    private fun dialogSearchMember(){
        val dialog= Dialog(this)
        dialog.setContentView(R.layout.search_member)
        var addButton:TextView=dialog.findViewById(R.id.tv_add)
        var CancelButton:TextView=dialog.findViewById(R.id.tv_cancel)
        var emailSearchMember: AppCompatEditText=dialog.findViewById(R.id.et_email_search_member)


        addButton.setOnClickListener {
            val email=emailSearchMember.text.toString()
            if(email.isNotEmpty()){
                dialog.dismiss()
                showProgressDialog("Please wait")
                firestoreClass().getMemberDetails(this,email )

            }else{
                Toast.makeText(this@MembersActivity,
                    "Please enter members email address",Toast.LENGTH_SHORT).show()
            }

        }
        CancelButton.setOnClickListener {
            dialog.dismiss()

        }
        dialog.show()


    }

    override fun onBackPressed() {
        if(anyChangeMode){
            setResult(Activity.RESULT_OK)
        }
        super.onBackPressed()

    }
    fun memberAssignSuccess(user:User){
        hideProgressDialog()
        mAssignedMembersList.add(user)
        anyChangeMode=true
        setupMembersList(mAssignedMembersList )
        SendNotificationToUserAsyncTask(mBoardDetails.name, user.fcmToken ).execute()
    }

    private inner class SendNotificationToUserAsyncTask(val boardName:String,val token:String):
            AsyncTask<Any,Void,StringBufferInputStream>(){
        override fun onPreExecute() {
            super.onPreExecute()
            showProgressDialog("Please Wait")
        }
        override fun doInBackground(vararg params: Any): StringBufferInputStream? {
            var result:String
            var connection:HttpURLConnection?=null
            try {
                val url=URL(Constants.FCM_BASE_URL)
                connection=url.openConnection()as HttpURLConnection
                connection.doOutput=true
                connection.doInput=true
                connection.instanceFollowRedirects=false
                connection.requestMethod="POST"


                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("charset", "utf-8")
                connection.setRequestProperty("Accept", "application/json")

                connection.setRequestProperty(Constants.FCM_AUTHORIZATION,"${Constants.FCM_KEY}=${Constants.FCM_SERVER_KEY}")
                connection.useCaches=false
                val writer=DataOutputStream(connection.outputStream)
                val jsonRequest=JSONObject()
                val dataObject=JSONObject()
                dataObject.put(Constants.FCM_KEY_TITLE,"Assigned to the Board $boardName")
                dataObject.put(Constants.FCM_KEY_MESSAGE,"you have been assigned to the board by ${mAssignedMembersList[0].name}")
                jsonRequest.put(Constants.FCM_KEY_DATA,dataObject)
                jsonRequest.put(Constants.FCM_KEY,token)
                writer.writeBytes(jsonRequest.toString())
                writer.flush()
                writer.close()
                val httpResult:Int=connection.responseCode
                if(httpResult==HttpURLConnection.HTTP_OK){
                    val inputStream=connection.inputStream
                    val reader =BufferedReader(
                        InputStreamReader(inputStream))
                    val sb = StringBuilder()
                    var line :String?
                    try {
                        while (reader.readLine().also { line=it }!=null){
                            sb.append(line+"\n")
                        }
                    }catch (e:IOException){
                        e.printStackTrace()
                    }finally {
                        try {
                            inputStream.close()
                        }catch (e:IOException){
                            e.printStackTrace()
                        }
                    }
                    result=sb.toString()


                }else{
                    result=connection.responseMessage
                }


            }catch (e:SocketTimeoutException){
                result="Connection Timeout"
            }catch (e:Exception){
                result="Error"+e.message
            }finally {
                connection?.disconnect()
            }
            return StringBufferInputStream(result)
        }

        override fun onPostExecute(result: StringBufferInputStream?) {
            super.onPostExecute(result)
            hideProgressDialog()
        }

    }
}
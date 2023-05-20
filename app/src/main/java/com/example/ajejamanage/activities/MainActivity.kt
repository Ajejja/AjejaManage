package com.example.ajejamanage.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.SyncStateContract
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ajejamanage.R
import com.example.ajejamanage.adapters.BoardItemsAdapter
import com.example.ajejamanage.firebase.firestoreClass
import com.example.ajejamanage.models.Board
import com.example.ajejamanage.models.User
import com.example.ajejamanage.utils.Constants
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    private val toolbarMainAct: Toolbar by lazy { findViewById<Toolbar>(R.id.toolbarMainAct) }
    private val drawer_layout: DrawerLayout by lazy { findViewById<DrawerLayout>(R.id.drawer_layout) }
    private val navView: NavigationView by lazy { findViewById<NavigationView>(R.id.nav_view) }
    private val nav_user_image:ImageView by lazy { findViewById(R.id.nav_user_image) }
    private val tv_userName:TextView by lazy { findViewById(R.id.tv_userName)}
    private val btnCreate: FloatingActionButton by lazy{findViewById(R.id.btn_create_new_board)}
    private var mBoardList: ArrayList<Board> = ArrayList()
    private lateinit var mUserName:String
    private lateinit var rv_boards_lists: RecyclerView
    private lateinit var mSharedPreferences:SharedPreferences



    companion object{
        const val MY_PROFILE_REQUEST_CODE:Int=11
        const val CREATE_BOARD_REQUEST_CODE = 12
        const val TASK_ACTIVITY_REQUEST_CODE=13


    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        rv_boards_lists = findViewById(R.id.rv_boards_lists)

        setupActionBar()
        navView.setNavigationItemSelectedListener(this)
        mSharedPreferences=this.getSharedPreferences(Constants.AJEJAMANAGE_PREFERENCES,Context.MODE_PRIVATE)
        val tokenUpdated=mSharedPreferences.getBoolean(Constants.FCM_TOKEN_UPDATED,false)
        if (tokenUpdated){
            showProgressDialog("Please Wait")
            firestoreClass().LoadUserData(this,true)
        }else {
            FirebaseMessaging.getInstance().token.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    updateFcmToken(token)
                }
            }
        }



        firestoreClass().LoadUserData(this,true)
        btnCreate.setOnClickListener {
            val intent=Intent(this,CreateBoardActivity::class.java)
            intent.putExtra(Constants.Name, mUserName)
            startActivityForResult(intent, CREATE_BOARD_REQUEST_CODE)
        }

    }
    fun tokenUpdateSuccess(){
        hideProgressDialog()
        val editor:SharedPreferences.Editor=mSharedPreferences.edit()
        editor.putBoolean(Constants.FCM_TOKEN_UPDATED,true)
        editor.apply()
        showProgressDialog("PLease Wait")
        firestoreClass().LoadUserData(this,true)
    }
    private fun updateFcmToken(token:String){
        val userHashmap=HashMap<String,Any>()
        userHashmap[Constants.FCM_TOKEN]=token
        showProgressDialog("PLease Wait")
        firestoreClass().updateUserProfileDate(this,userHashmap)

    }
    fun populateBoardsListToUI(boardsList:ArrayList<Board>){
        val NoBoards: TextView by lazy { findViewById(R.id.tv_NoBoardsAvailable)}

        hideProgressDialog()
        if(boardsList.size>0){
            rv_boards_lists.visibility= View.VISIBLE
            NoBoards.visibility=View.GONE
            rv_boards_lists.layoutManager= LinearLayoutManager(this@MainActivity)
            rv_boards_lists.setHasFixedSize(true)
            val adapter=BoardItemsAdapter(this@MainActivity,boardsList)
            rv_boards_lists.adapter=adapter
            adapter.setOnClickListener(object :BoardItemsAdapter.OnClickListener{
                override fun onClick(position: Int, model: Board) {
                    val intent =Intent(this@MainActivity,TaskActivity::class.java)
                    intent.putExtra(Constants.DOCUMENT_ID,model.documentId)
                    startActivity(intent)
                }
            })



        }else{
            rv_boards_lists.visibility= View.GONE
            NoBoards.visibility=View.VISIBLE
        }
    }
    fun updateNavigationUserDetails(user: User, readBoardsList: Boolean) {
        hideProgressDialog()
        mUserName = user.name

        if (!isFinishing) {
            Glide.with(this@MainActivity)
                .load(user.image)
                .centerCrop()
                .placeholder(R.drawable.ic_user_place_holder)
                .into(nav_user_image)
        }

        tv_userName.text = user.name
        if (readBoardsList) {
            if (!isFinishing) {
                showProgressDialog("Please Wait")
                firestoreClass().getBoardsList(this)
            }
        }
    }



    private fun setupActionBar() {
        setSupportActionBar(toolbarMainAct)
        toolbarMainAct.setNavigationIcon(R.drawable.ic_action_navigation_menu)
        toolbarMainAct.setNavigationOnClickListener {
            toggleDrawer()
        }
    }

    private fun toggleDrawer() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            drawer_layout.openDrawer(GravityCompat.START)
        }
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            doubleBackToExit()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == MY_PROFILE_REQUEST_CODE) {
            } else if (requestCode == CREATE_BOARD_REQUEST_CODE) {
                // Handle the result from CreateBoardActivity
                getBoardsListFromFireStore()
            } else if (requestCode == TASK_ACTIVITY_REQUEST_CODE) {
                getBoardsListFromFireStore()
            } else {
                Log.e("Cancelled", "Cancelled")
            }
        } else {
            Log.e("Cancelled", "Cancelled")
        }
    }




    fun getBoardsListFromFireStore() {
        showProgressDialog("Please Wait")
        firestoreClass().getBoardsList(this@MainActivity)
        startActivityForResult(intent, TaskActivity.BOARD_REQUEST_CODE)

    }






    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.navMyProfile -> {
                startActivityForResult(Intent(this,MyProfileActivity::class.java),
                    MY_PROFILE_REQUEST_CODE)
            }
            R.id.navSignOut -> {
                mSharedPreferences.edit().clear().apply()
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, IntroActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

}
package com.example.ajejamanage.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.example.ajejamanage.activities.CardDetailsActivity
import com.example.ajejamanage.activities.CreateBoardActivity
import com.example.ajejamanage.activities.MainActivity
import com.example.ajejamanage.activities.MembersActivity
import com.example.ajejamanage.activities.MyProfileActivity
import com.example.ajejamanage.activities.SignInActivity
import com.example.ajejamanage.activities.SignUpActivity
import com.example.ajejamanage.activities.TaskActivity
import com.example.ajejamanage.models.Board
import com.example.ajejamanage.models.User
import com.example.ajejamanage.utils.Constants
import com.google.android.play.core.integrity.e
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class firestoreClass {
    private var mFireStore=FirebaseFirestore.getInstance()
    private var mBoardList: ArrayList<Board> = ArrayList()
    fun registerUser(activity: SignUpActivity, userInfo: User) {
        mFireStore.collection(Constants.USERS)


        mFireStore.collection(Constants.USERS).document(getCurrentUserId()).set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.userRegisteredSuccess()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error writing document", e)
            }
    }

    fun LoadUserData(activity: Activity, readBoardsList: Boolean = false) {
        mFireStore.collection(Constants.USERS).document(getCurrentUserId()).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists() && !activity.isFinishing) {
                    Log.e(activity.javaClass.simpleName, document.toString())
                    val loggedInUser = document.toObject(User::class.java)!!
                    when (activity) {
                        is SignInActivity -> {
                            activity.signInSuccess(loggedInUser)
                        }
                        is MainActivity -> {
                            activity.updateNavigationUserDetails(loggedInUser, readBoardsList)

                        }
                        is MyProfileActivity -> {
                            activity.setUserDataInUI(loggedInUser)
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                if (!activity.isFinishing) {
                    when (activity) {
                        is SignInActivity -> {
                            activity.hideProgressDialog()
                        }
                        is MainActivity -> {
                            activity.hideProgressDialog()
                        }
                        is MyProfileActivity -> {
                            activity.hideProgressDialog()
                        }
                    }
                    Log.e(activity.javaClass.simpleName, "Error retrieving user data", e)
                    Toast.makeText(activity, "Error retrieving user data", Toast.LENGTH_SHORT).show()
                }
            }
    }



    fun getBoardsList(activity: MainActivity) {
        mFireStore.collection(Constants.Boards)
            .whereArrayContains(Constants.assignedTo, getCurrentUserId())
            .get()
            .addOnSuccessListener { document ->
                Log.i(activity.javaClass.simpleName, document.documents.toString())
                val boardList: ArrayList<Board> = ArrayList()
                for (i in document.documents) {
                    val board = i.toObject(Board::class.java)!!
                    board.documentId = i.id
                    boardList.add(board)
                }
                Log.i(activity.javaClass.simpleName, "Boards list size: ${boardList.size}")
                mBoardList = boardList
                activity.populateBoardsListToUI(mBoardList)

            }
            .addOnFailureListener { e ->
                Log.e(activity.javaClass.simpleName, "Error while getting the list of boards.", e)
                activity.hideProgressDialog()
            }
    }
    fun createBoard(activity: CreateBoardActivity, board: Board) {
        mFireStore.collection(Constants.Boards).document().set(board, SetOptions.merge()).
        addOnSuccessListener{
            Log.e(activity.javaClass.simpleName,"Board Created Successfully")
            Toast.makeText(activity, "Board Created Successfully!", Toast.LENGTH_SHORT).show()
            activity.boardCreatedSuccessfully()
        }.addOnFailureListener { e->
            activity.hideProgressDialog()
            Log.e(activity.javaClass.simpleName,"Error while creating board.",e)
        }
    }

    fun addUpdateTaskList(activity: Activity,board:Board){
         val taskListHashMAp=HashMap<String,Any>()
        taskListHashMAp[Constants.TASK_LIST]=board.taskList
        mFireStore.collection(Constants.Boards).document(board.documentId).update(taskListHashMAp).addOnSuccessListener {
            Log.e(activity.javaClass.simpleName,"TaskList updated successfully")
            if(activity is TaskActivity)
                activity.addUpdateTaskListSuccess()
            else if (activity is CardDetailsActivity)
                activity.addupdateTaskListSuccess()
        }.addOnFailureListener { e ->
            if(activity is TaskActivity)
                activity.hideProgressDialog()
            else if (activity is CardDetailsActivity)
                activity.hideProgressDialog()


            Log.e(activity.javaClass.simpleName, "Error while creating a board.", e)

        }
    }
    fun getAssignedMembersListDetails(activity: Activity, assignedTo: ArrayList<String>) {
        Log.d("firestoreClass", "Assigned users: $assignedTo")
        mFireStore.collection(Constants.USERS)
            .whereIn(Constants.ID, assignedTo)
            .get()
            .addOnSuccessListener { documents ->
                val usersList: ArrayList<User> = ArrayList()
                for (document in documents) {
                    val user = document.toObject(User::class.java)
                    Log.d("firestoreClass", "Fetched user: $user")
                    usersList.add(user)
                }
                if(activity is MembersActivity)
                    activity.setupMembersList(usersList)
                else if (activity is TaskActivity)
                    activity.boardMembersDetailsList(usersList)
            }
            .addOnFailureListener { e ->
                if(activity is MembersActivity)
                    activity.hideProgressDialog()
                else if (activity is TaskActivity)
                    activity.hideProgressDialog()
                Log.e("firestoreClass", "Error while fetching assigned members list.", e)

            }
    }


    fun getBoardDetails(activity:TaskActivity,documentId:String){
        mFireStore.collection(Constants.Boards)
            .document(documentId)
            .get()
            .addOnSuccessListener { document ->
                Log.i(activity.javaClass.simpleName, document.toString())
                val board=document.toObject(Board::class.java)!!
                board.documentId=document.id
                activity.boardDetails(board )


            }
            .addOnFailureListener { e ->
                Log.e(activity.javaClass.simpleName, "Error while getting the list of boards.", e)
                activity.hideProgressDialog()
            }

    }



    fun updateUserProfileDate(activity: Activity, userHashMap: HashMap<String, Any>) {
        mFireStore.collection(Constants.USERS).document(getCurrentUserId()).update(userHashMap)
            .addOnSuccessListener {
                Log.i(activity.javaClass.simpleName, "Profile Date Updated Successfully!")
                Toast.makeText(activity, "Profile Updated Successfully!", Toast.LENGTH_SHORT).show()
                when(activity){
                    is MainActivity->{
                        activity.tokenUpdateSuccess()
                    }
                     is MyProfileActivity->{
                        activity.profileUpdateSuccess()
               }
                }
            }.addOnFailureListener { e ->
                when(activity){
                    is MainActivity->{
                activity.hideProgressDialog()
                    }
                    is MyProfileActivity->{
                activity.hideProgressDialog()    }
            }
            }
                Log.e(activity.javaClass.simpleName, "Error while updating the profile.")



    }
    fun getMemberDetails(activity: MembersActivity,email:String){
        mFireStore.collection(Constants.USERS).whereEqualTo(Constants.EMAIL,email).get().addOnSuccessListener {
            documents->
            if(documents.documents.size>0){
                val user=documents.documents[0].toObject(User::class.java)!!

                activity.memberDetails(user)
            }else{
                activity.hideProgressDialog()
                activity.showErrorSnackBar("No such member found!!")
            }
        }.addOnFailureListener {
            activity.hideProgressDialog()
            Log.e(
                activity.javaClass.simpleName,"Error while getting user details" )


        }
    }
    fun assignMemebersToBoard(activity: MembersActivity,board: Board,user: User){
        val assignedToHashMap=HashMap<String,Any>()
        assignedToHashMap[Constants.assignedTo]=board.assignedTo
        mFireStore.collection(Constants.Boards)
            .document(board.documentId)
            .update(assignedToHashMap)
            .addOnSuccessListener {
                activity.memberAssignSuccess(user)

            }.addOnFailureListener {
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName,"Error while creating board")
            }

    }
    fun deleteBoard(activity: TaskActivity, documentId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection(Constants.Boards)
            .document(documentId)
            .delete()
            .addOnSuccessListener {
                activity.boardDeletedSuccessfully()
                activity.setResult(Activity.RESULT_OK)
                activity.finish()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error deleting board", e)
            }
    }




    fun getCurrentUserId():String{
        var currentUser=FirebaseAuth.getInstance().currentUser
        var currentUserId=""
        if(currentUser!=null){
            currentUserId=currentUser.uid
        }
        return currentUserId

    }
}
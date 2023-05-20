package com.example.ajejamanage.activities

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.example.ajejamanage.R
import com.example.ajejamanage.firebase.firestoreClass
import com.example.ajejamanage.models.User
import com.example.ajejamanage.utils.Constants
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import java.io.IOException
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView

class MyProfileActivity : BaseActivity() {

    private var mSelectedImageFileUri: Uri?=null
    private var isPermissionRequested = false
    private lateinit var mUserDetails:User
    private var mProfileImageURl:String=""
    private val toolbarprofile: Toolbar by lazy { findViewById<Toolbar>(R.id.toolbar_profile) }
    private val iv_userImage: ImageView by lazy { findViewById(R.id.iv_userImage) }
    private val userName: AppCompatEditText by lazy { findViewById(R.id.profile_name) }
    private val userEmail: AppCompatEditText by lazy { findViewById(R.id.user_email) }
    private val userMobile: AppCompatEditText by lazy { findViewById(R.id.user_mobile_num) }
    private val btn_Update: MaterialButton by lazy { findViewById(R.id.btn_Update) }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)
        setupActionBar()
        firestoreClass().LoadUserData(this)
        iv_userImage.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                Constants.showImageChooser(this)
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    Constants.READ_STORAGE_PERMISSION_CODE
                )
            }
        }
        btn_Update.setOnClickListener {
            if(mSelectedImageFileUri!=null){
                uploadUserImage()
            }else{
                showProgressDialog("Please wait")
                updateUserProfileData()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.READ_STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Constants.showImageChooser(this)
            } else {
                if (!isPermissionRequested) {
                    isPermissionRequested = true
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        Constants.READ_STORAGE_PERMISSION_CODE
                    )
                } else {
                    Toast.makeText(
                        this,
                        "Oops, you just denied the permission for storage. You can allow it from the settings",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }
        }
    }




    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode==Activity.RESULT_OK&&requestCode== Constants.PICK_IMAGE_REQUEST_CODE&&data!!.data!=null){
            mSelectedImageFileUri=data.data
            try {
                Glide
                    .with(this@MyProfileActivity)
                    .load(mSelectedImageFileUri)
                    .centerCrop()
                    .placeholder(R.drawable.ic_user_place_holder)
                    .into(iv_userImage)
            }catch (e:IOException){
                e.printStackTrace()
            }
        }
    }

    private fun setupActionBar() {
        setSupportActionBar(toolbarprofile)
        var actionBar=supportActionBar
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_ios_23)
            actionBar.title="My Profile"

        }
        toolbarprofile.setNavigationOnClickListener {
            onBackPressed()
        }

    }
    fun setUserDataInUI(user:User){
        mUserDetails=user
        Glide
            .with(this@MyProfileActivity)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(iv_userImage)
        userName.setText(user.name)
        userEmail.setText(user.email)
        if(user.mobile!=0L){


        userMobile.setText(user.mobile.toString())
    }
    }
    private fun updateUserProfileData(){
        val userHashMap=HashMap<String,Any>()
        if(mProfileImageURl.isNotEmpty()&&mProfileImageURl!=mUserDetails.image){
            userHashMap["image"]
            userHashMap[Constants.Image]=mProfileImageURl
        }
        if(userName.text.toString()!=mUserDetails.name){
            userHashMap[Constants.Name]=userName.text.toString()

        }
        if(userMobile.text.toString().toLongOrNull() ?: 0 != mUserDetails.mobile){
            userHashMap[Constants.Mobile]=userMobile.text.toString().toLongOrNull() ?: 0
        }
        firestoreClass().updateUserProfileDate(this , userHashMap)
        firestoreClass().LoadUserData(MainActivity(), true)

    }
    private fun uploadUserImage(){
        showProgressDialog("Please wait")
        if(mSelectedImageFileUri!=null){
            val sRef: StorageReference = FirebaseStorage.getInstance().reference.child("USER_IMAGE"
                    +System.currentTimeMillis()+"."+Constants.getFileExtension(this,mSelectedImageFileUri))
            sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener {
                taskSnapshot->Log.e("Firebase Image URL",taskSnapshot.metadata!!.reference!!.downloadUrl.toString())
                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                    uri->
                    Log.i("Downloadable Image URL",uri.toString())
                    mProfileImageURl=uri.toString()
                    updateUserProfileData()


                }

            }.addOnFailureListener{
                exception->
                Toast.makeText(this@MyProfileActivity,exception.message,Toast.LENGTH_LONG).show()
                hideProgressDialog()
            }


        }
    }

    fun profileUpdateSuccess() {
        hideProgressDialog()
        val resultIntent = Intent()
        resultIntent.putExtra("updatedUser", mUserDetails)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }


}
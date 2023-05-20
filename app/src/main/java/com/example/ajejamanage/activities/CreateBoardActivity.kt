package com.example.ajejamanage.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.ajejamanage.R
import com.example.ajejamanage.firebase.firestoreClass
import com.example.ajejamanage.models.Board
import com.example.ajejamanage.utils.Constants
import com.google.android.material.button.MaterialButton
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException
import android.Manifest.permission
import androidx.activity.result.contract.ActivityResultContracts


class CreateBoardActivity : BaseActivity() {
    private var isPermissionRequested = false
    private var previousPackageName: String? = null

    private val ivCreateBoardImage: ImageView by lazy { findViewById(R.id.iv_CreateBoardImage) }
    private val boardName: AppCompatEditText by lazy { findViewById(R.id.board_name) }
    private val btn_Create: MaterialButton by lazy { findViewById(R.id.btn_Create) }



    private var mSelectedImageFileUri: Uri?=null
    private lateinit var mUserName:String

    private lateinit var videoView: VideoView
    private val toolbarcreate: Toolbar by lazy { findViewById<Toolbar>(R.id.toolbar_create) }
    private var mBoardImageUrl:String=""
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            Constants.showImageChooser(this)
        } else {
            Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_creat_board)


        videoView = findViewById(R.id.video_view2)
        val videoUri = Uri.parse("android.resource://" + packageName + "/" + R.raw.videoviewboard)
        videoView.setVideoURI(videoUri)
        videoView.setOnPreparedListener {
            it.isLooping = true
            it.start()
        }
        setupActionBar()

        if(intent.hasExtra(Constants.Name)){
            mUserName=intent.getStringExtra(Constants.Name)!!
        }
        ivCreateBoardImage.setOnClickListener {
            requestStoragePermission()
        }

        btn_Create.setOnClickListener {
            if (mSelectedImageFileUri != null) {
                uploadBoardImage()
            } else {
                showProgressDialog("PleaseWait")
                createBoard()
            }
        }


        btn_Create.setOnClickListener {
            if(mSelectedImageFileUri!=null){
                uploadBoardImage()
            }else{
                showProgressDialog("PleaseWait")
                createBoard()
            }
        }

    }
    private fun requestStoragePermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                Constants.showImageChooser(this)
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) -> {
                showInContextUI()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }
    private fun showInContextUI() {
        Toast.makeText(this, "Storage permission is required to choose an image", Toast.LENGTH_SHORT).show()
    }

    private fun createBoard(){
        val assignedUserArrayList:ArrayList<String> = ArrayList()
        assignedUserArrayList.add(getCurrentUserId())
        var board=Board(
            boardName.text.toString(),
            mBoardImageUrl,
            mUserName,
            assignedUserArrayList
        )
        firestoreClass().createBoard(this,board)
    }
    private fun uploadBoardImage(){
        showProgressDialog("Please Wait")
        val sRef: StorageReference = FirebaseStorage.getInstance().reference.child("BOARD_IMAGE"
                +System.currentTimeMillis()+"."+Constants.getFileExtension(this,mSelectedImageFileUri))
        sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener {
                taskSnapshot->
            Log.e(" Board Image URL",taskSnapshot.metadata!!.reference!!.downloadUrl.toString())
            taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                    uri->
                Log.i("Downloadable Image URL",uri.toString())
                mBoardImageUrl=uri.toString()
                createBoard()


            }

        }.addOnFailureListener{
                exception->
            Toast.makeText(this,exception.message,Toast.LENGTH_LONG).show()
            hideProgressDialog()
        }
    }
    fun boardCreatedSuccessfully() {
        hideProgressDialog()
        val resultIntent = Intent().apply {
            putExtra("boardCreated", true)
        }
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        super.onBackPressed()
    }


    private fun setupActionBar() {
        setSupportActionBar(toolbarcreate)
        var actionBar=supportActionBar
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_ios_23)
            actionBar.title="My Profile"

        }
        toolbarcreate.setNavigationOnClickListener {
            onBackPressed()
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
        if(resultCode== Activity.RESULT_OK&&requestCode== Constants.PICK_IMAGE_REQUEST_CODE&&data!!.data!=null){
            mSelectedImageFileUri=data.data
            try {
                Glide
                    .with(this)
                    .load(mSelectedImageFileUri)
                    .centerCrop()
                    .placeholder(R.drawable.baseline_circle_24)
                    .into(ivCreateBoardImage)
            }catch (e: IOException){
                e.printStackTrace()
            }
        }
    }
}
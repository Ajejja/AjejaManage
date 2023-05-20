package com.example.ajejamanage.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.WindowManager
import android.widget.Toast
import com.example.ajejamanage.R
import com.example.ajejamanage.databinding.ActivitySignUpBinding
import com.example.ajejamanage.firebase.firestoreClass
import com.example.ajejamanage.models.User
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class SignUpActivity : BaseActivity() {
    private lateinit var binding: ActivitySignUpBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
        }

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        binding.btnSignUpAct.setOnClickListener{
            registerUser()
        }
        setupActionBar()
    }


    private fun setupActionBar(){
        setSupportActionBar(binding.toolbarSignUp)
        val actionBar=supportActionBar
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_ios_24)
        }

    }
    fun userRegisteredSuccess(){
        Toast.makeText(
            this,

                    "you have sucessfully registeded ",
            Toast.LENGTH_LONG
        ).show()
        hideProgressDialog()
        FirebaseAuth.getInstance().signOut()
        finish()

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
    private fun registerUser (){
        val name: String = binding.etName.text.toString().trim{it<=' ' }
        val email: String = binding.etEmail.text.toString().trim{it<=' '}
        val password: String = binding.etPassword.text.toString().trim{it<=' '}
        if (validateForm(name,email,password)){
            showProgressDialog("Please wait")
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password).addOnCompleteListener { task ->
                hideProgressDialog()
                if (task.isSuccessful) {
                    val firebaseUser: FirebaseUser = task.result!!.user!!
                    val registeredEmail = firebaseUser.email!!
                    val user= User(firebaseUser.uid,name,registeredEmail)
                    firestoreClass().registerUser(this,user)
                } else {
                    Toast.makeText(this, task.exception!!.message, Toast.LENGTH_SHORT).show()
                }
            }
        }


        }
    private fun validateForm(name:String,email:String,password:String):Boolean{
        return when {
            TextUtils.isEmpty(name)->{
                showErrorSnackBar("Please enter a name")
                 false
            }
            TextUtils.isEmpty(email)->{
                showErrorSnackBar("Please enter an email adress ")
                 false}
            TextUtils.isEmpty(password)->{
                showErrorSnackBar("Please enter your password")
                 false}else->{
                 true

                }
        }
    }
}
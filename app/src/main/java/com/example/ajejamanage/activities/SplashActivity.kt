package com.example.ajejamanage.activities

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import android.widget.VideoView
import com.example.ajejamanage.R
import com.example.ajejamanage.firebase.firestoreClass

class SplashActivity : AppCompatActivity() {

    private lateinit var videoView: VideoView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        videoView = findViewById(R.id.video_view)
        val videoUri = Uri.parse("android.resource://" + packageName + "/" + R.raw.backgroundvideo)
        videoView.setVideoURI(videoUri)
        videoView.setOnPreparedListener {
            it.isLooping = true
            it.start()
           Handler().postDelayed({
               var currentUserId=firestoreClass().getCurrentUserId()
               if(currentUserId.isNotEmpty()){
                   startActivity(Intent(this,MainActivity::class.java))
               }else{
               startActivity(Intent(this, IntroActivity::class.java))
               finish()
               }
           },3000)
        }
    }
}

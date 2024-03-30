package com.example.flowchart2code

// SplashActivity.kt
import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.activity.ComponentActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)

        val imageView: ImageView = findViewById(R.id.imageView)

        // Animation for fading in the splash screen
        val fadeIn = AnimationUtils.loadAnimation(applicationContext, R.anim.fade_in)
        imageView.startAnimation(fadeIn)

        // Delay for 3 seconds and then start the MainActivity
        GlobalScope.launch(Dispatchers.Main) {
            delay(2000)
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()
        }
    }
}

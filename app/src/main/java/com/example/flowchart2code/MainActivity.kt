package com.example.flowchart2code

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main);

//        // Reference the button
//        val flowchart2CodeButton: Button = findViewById(R.id.Flowchart2Code)

        // Reference the button
        val codeToFlowchartButton: Button = findViewById(R.id.Code2Flowchart)

        // Find the button by its ID
        val trylButton = findViewById<Button>(R.id.trylButton)



//        // Set up click listener for the button
//        flowchart2CodeButton.setOnClickListener {
//            // Create an intent to navigate to the destination activity
//            val intent = Intent(this@MainActivity, flowcharttoc::class.java)
//            startActivity(intent)
//        }

        // Set up click listener for the button
        codeToFlowchartButton.setOnClickListener {
            // Create an intent to navigate to the destination activity
            val intent = Intent(this@MainActivity, CodeToFlowchart::class.java)
            startActivity(intent)
        }

        // Set OnClickListener for the button
        trylButton.setOnClickListener {
            // Open the Tryl activity when the button is clicked
            val intent = Intent(this@MainActivity, Tryl::class.java)
            startActivity(intent)
        }
    }
}
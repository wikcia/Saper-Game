package com.example.saper

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Chronometer

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val startGame = findViewById<Button>(R.id.start_game)
        startGame?.setOnClickListener {
            val intent = Intent(this, GameLogic::class.java)
            startActivity(intent)
        }

    }
}
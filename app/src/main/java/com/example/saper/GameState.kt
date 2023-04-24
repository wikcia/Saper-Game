package com.example.saper

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView

class GameState : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_state)

        val trophy = findViewById<ImageView>(R.id.trophy)
        val congrats = findViewById<TextView>(R.id.congrats)
        val gameMessage = findViewById<TextView>(R.id.game_message)
        val continuePlay = findViewById<Button>(R.id.continue_play)


        val result = intent.getStringExtra("result")
        if(result=="Lose")
        {
            trophy.setImageResource(R.drawable.lost)
            congrats.text = getString(R.string.lost_message)
            gameMessage.text = getString(R.string.game_loss)

        }
        else if(result=="Win")
        {
            trophy.setImageResource(R.drawable._5485)
            congrats.text = getString(R.string.congratulations)
            gameMessage.text = getString(R.string.win_message)

        }

        continuePlay?.setOnClickListener {
            val intent1= Intent(this, MainActivity::class.java)
            startActivity(intent1)
        }

    }
}
package com.example.saper

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.widget.Chronometer
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import kotlin.random.Random

class GameLogic : AppCompatActivity() {

    private var flaggedmines = 0

    private var firstMove = true
    private var status = Status.ONGOING
    private var rows = 0
    private var columns = 0
    private var mines = 0
    private var mineboard = Array(rows) { Array(columns) { Cell(this) } } // tablica dwuwymiarowa pól gry
    private var playStarted = false
    private var played = 0
    private var won = 0
    private var lost = 0
    private var lastGameTime = Integer.MAX_VALUE
    private var fastestTime = Integer.MAX_VALUE

    private lateinit var timer : Chronometer
    private lateinit var minesLeftInfo : TextView
    private lateinit var restart : AppCompatButton
    private lateinit var choice :ImageButton
    private lateinit var board : LinearLayout


    /**
     * By declaring the companion object,
     * you can access the members of the class by class name only
     * (which is without explicitly creating the instance of the class).
     */
    companion object {
        const val MINE = -1
        const val reveal = "reveal"
        const val flag = "flag"
        val movement = arrayOf(-1, 0, 1)
    }

    private var toMove: String = reveal

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_logic)

        timer = findViewById(R.id.timer)
        minesLeftInfo = findViewById(R.id.minesLeftinfo)
        restart = findViewById(R.id.restart)
        choice = findViewById(R.id.choice)
        board = findViewById(R.id.board)

        //starts the timer
        if (!playStarted) {
            timer.base = SystemClock.elapsedRealtime()
            timer.start()
            Toast.makeText(this, "Gra się rozpoczęła!", Toast.LENGTH_SHORT).show()
            playStarted = true //!!!!!!!!!!!!
        }

        // ustawiamy wielkość planszy i liczbę bomb
        rows = 9
        columns = 9
        mines = 10


       //komunikat ile zostało nieoflagowanych bomb
        minesLeftInfo.text = mines.toString()

        //create board function will create the layout as per dimensions
        createBoard()

        // popup pytający użytkownika czy na pewno chce wyjść z gry
        restart.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            with(builder)
            {
                setTitle("Zrestartuj grę")
                setMessage("Czy na pewno chcesz zacząć od nowa?")
                setPositiveButton("Tak", DialogInterface.OnClickListener { _, _ ->
                    val intents = Intent(this@GameLogic, GameLogic::class.java) //!!!!!!!!!!!!!!!!!!!!!
                    finish()
                    startActivity(intents)
                })
                setNegativeButton(
                    "Nie"
                ) { _, _ -> }
            }
            val alert = builder.create()
            alert.show()
        }
    }


    /**
     * Funkcja tworząca planszę do gry
     */
    private fun createBoard() {
        mineboard = Array(rows) { Array(columns) { Cell(this) } } // dwuwymiarowa tablica


        val params1 = LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.MATCH_PARENT
        )

        val params2 = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0
        )

        var id = 1
        // po nakliknięciu przycisku wyboru zmienia się obrazek albo z flagi na bombę albo na odwrót
        choice.setOnClickListener {

            if (toMove == reveal) {
                toMove = flag
                choice.setImageResource(R.drawable.flag)

            } else {
                toMove = reveal
                choice.setImageResource(R.drawable.bomb)
            }
        }

        for (i in 0 until rows) {
            val linearLayout = LinearLayout(this)
            linearLayout.orientation = LinearLayout.HORIZONTAL
            linearLayout.layoutParams = params2
            params2.weight = 1.0F

            for (j in 0 until columns) {
                val button = Cell(this)
                mineboard[i][j] = button
                button.id = id

                id++
                button.layoutParams = params1
                params1.weight = 1.0F
                button.setBackgroundResource(R.drawable.notopenednew)

                linearLayout.addView(button)

                button.setOnClickListener {
                    // if the user ha clicked the cell first time , the mines will be setup in the game ensuring the first clicked cell isnt a mine/bomb.
                    if (firstMove) {
                        firstMove = false

                        setupMines(i, j)
                    }
                    move(toMove, i, j)
                    displayBoard()
                    if (isComplete() && status != Status.LOST) {
                        status = Status.WON
                        finalResult()
                    }
                }


            }
            board.addView(linearLayout)
        }
    }

    //used to display the cells
    private fun displayBoard() {
        mineboard.forEach { row ->
            row.forEach {
                if (it.isRevealed)
                    setImage(it)
                else if (it.isFlagged) {

                    if (status == Status.LOST && !it.isMine)
                        it.setBackgroundResource(R.drawable.crossedflag)
                    else
                        it.setBackgroundResource(R.drawable.flag)
                } else if (status == Status.LOST && it.value == MINE)
                    it.setBackgroundResource(R.drawable.mine)
                else if (status == Status.WON && it.value == MINE)
                    it.setBackgroundResource(R.drawable.flag)
                else
                    it.text = " "
            }

        }
    }

    /**
     * funkcja ustawiająca obrazek pojedynczej komórki
     */
    private fun setImage(cell: Cell) {
        with(cell)
        {
            when (value) {
                0 -> setBackgroundResource(R.drawable.zero)
                1 -> setBackgroundResource(R.drawable.one)
                2 -> setBackgroundResource(R.drawable.two)
                3 -> setBackgroundResource(R.drawable.three)
                4 -> setBackgroundResource(R.drawable.four)
                5 -> setBackgroundResource(R.drawable.five)
                6 -> setBackgroundResource(R.drawable.six)
                7 -> setBackgroundResource(R.drawable.seven)
                8 -> setBackgroundResource(R.drawable.eight)
            }
        }
    }


    // reveals or flags the cell as per option chosen by user
    private fun move(choice: String, x: Int, y: Int): Boolean {

        when (choice) {
            reveal -> {
                if (mineboard[x][y].isFlagged || mineboard[x][y].isRevealed)
                    return false

                if (mineboard[x][y].value == MINE) {
                    status = Status.LOST
                    finalResult()
                    return true
                } else
                    reveal(x, y)
                return true
            }

            flag -> {
                with(mineboard[x][y])
                {
                    if (isRevealed)
                        return false
                    else if (isFlagged) {
                        isFlagged = false
                        setBackgroundResource(R.drawable.notopenednew)
                        flaggedmines--
                        minesLeftInfo.text = (mines - flaggedmines).toString()
                        return true

                    } else {
                        if (flaggedmines == mines) {
                            Toast.makeText(
                                this@GameLogic,
                                "Nie możesz oflagować więcej pól",
                                Toast.LENGTH_SHORT
                            ).show()
                            return false
                        } else {
                            isFlagged = true
                            flaggedmines++
                            setBackgroundResource(R.drawable.flag)
                            minesLeftInfo.text = (mines - flaggedmines).toString()
                            return true
                        }
                    }
                }
            }
        }
        return true
    }


    private fun finalResult() {
        //timer stops as soon as game finishes
        timer.stop()

        if (status == Status.WON) {

            // za pomocą intencji przekazujemy aktywności game state odpowiedni komunikat
            val intent = Intent(this, GameState::class.java).apply {
                putExtra("result", "Won")
            }
            startActivity(intent)
        } else if (status == Status.LOST) {

            val intent = Intent(this, GameState::class.java).apply {
                putExtra("result", "Lose")
            }
            startActivity(intent)
        }

    }

    // sprawdza warunki wygranej gry
    private fun isComplete(): Boolean {
        var minesMarked = true
        var valuesRevealed = true
        mineboard.forEach { row ->
            row.forEach {
                if (it.value == MINE) {
                    if (!it.isFlagged)
                        minesMarked = false
                } else if (it.value != MINE) {
                    if (!it.isRevealed)
                        valuesRevealed = false
                }
            }

        }
        if (minesMarked && valuesRevealed) {
            status = Status.WON
            return true
        }

        return false
    }


    private fun reveal(x: Int, y: Int) {

        if (!mineboard[x][y].isRevealed && !mineboard[x][y].isFlagged && mineboard[x][y].value != -1) {
            mineboard[x][y].isRevealed = true
            if (mineboard[x][y].value == 0) {
                for (i in movement)
                    for (j in movement)
                        if ((i != 0 || j != 0) && ((x + i) in 0 until rows) && ((y + j) in 0 until columns))
                            reveal(x + i, y + j)
            }
        }
    }


    //losowo wybieramy miejsca na bomby, w onCreate() ustawiamy ich liczbę na 10
    private fun setupMines(i: Int, j: Int) {
        var m = 1

        while (m <= mines) {
            val x = Random.nextInt(0, rows)
            val y = Random.nextInt(0, columns)
            if (((x in i - 1..i + 1) && (y in j - 1..j + 1)) || mineboard[x][y].isMine) {
                continue
            } else
                mineboard[x][y].value = -1
            mineboard[x][y].isMine = true
            updateNeighbours(x, y)
            m++
        }
    }

    // ta funkcja zwieksza wartości pól dookoła bomby
    // movement jako arrayOf(-1, 0, 1)
    private fun updateNeighbours(x: Int, y: Int) {
        for (i: Int in movement) {
            for (j : Int in movement) {
                if (((x + i) in 0 until rows) && ((y + j) in 0 until columns) && mineboard[x + i][y + j].value != -1) {
                    mineboard[x + i][y + j].value++
                }
            }
        }
    }
}



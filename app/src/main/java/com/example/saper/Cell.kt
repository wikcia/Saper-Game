package com.example.saper

import android.content.Context

/**
 * Klasa pojedynczej komórki w planszy
 * W taki sposób tworzymy konstruktor w Kotlinie, bezpośrednio w sygnaturze klasy
 */
class Cell constructor(context: Context) : androidx.appcompat.widget.AppCompatButton(context) {

    var value : Int = 0
    var isRevealed : Boolean = false
    var isFlagged = false
    var isMine = false

}
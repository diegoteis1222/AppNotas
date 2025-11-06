package com.example.notas

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class NoteDetailActivity : AppCompatActivity() {

    companion object {
        // Usamos una constante para la clave del dato que vamos a pasar.
        // Es una buena práctica para evitar errores de escritura.
        const val EXTRA_NOTE_TEXT = "EXTRA_NOTE_TEXT"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_detail)

        // 1. Encontrar el TextView en nuestro layout.
        val noteDetailTextView: TextView = findViewById(R.id.noteDetailTextView)

        // 2. Obtener el texto de la nota que nos envió la MainActivity.
        val noteText = intent.getStringExtra(EXTRA_NOTE_TEXT)

        // 3. Poner el texto en el TextView.
        noteDetailTextView.text = noteText
    }
}
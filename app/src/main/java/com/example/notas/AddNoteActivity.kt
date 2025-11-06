package com.example.notas

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class AddNoteActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_note)

        val nuevoTitulo = findViewById<EditText>(R.id.nuevoTitulo)
        val guardarNota = findViewById<Button>(R.id.guardarNota)
        val nuevaDescripcion = findViewById<EditText>(R.id.nuevaDescripcion)

        guardarNota.setOnClickListener {
            val noteTitle = nuevoTitulo.text.toString()
            val noteDescription = nuevaDescripcion.text.toString()

            val resultIntent = Intent()
            resultIntent.putExtra("note_title", noteTitle)
            resultIntent.putExtra("note_description", noteDescription)
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }
}

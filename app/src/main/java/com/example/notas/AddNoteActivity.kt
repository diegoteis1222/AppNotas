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

        val nuevaNota = findViewById<EditText>(R.id.nuevaNota)
        val guardarNota = findViewById<Button>(R.id.guardarNota)

        guardarNota.setOnClickListener {
            val noteText = nuevaNota.text.toString()
            val resultIntent = Intent()
            resultIntent.putExtra("note", noteText)
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }
}

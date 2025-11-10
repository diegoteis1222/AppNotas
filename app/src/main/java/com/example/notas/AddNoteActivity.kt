package com.example.notas

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import io.noties.markwon.Markwon
import io.noties.markwon.editor.MarkwonEditor
import io.noties.markwon.editor.MarkwonEditorTextWatcher
import io.noties.markwon.image.coil.CoilImagesPlugin

class AddNoteActivity : AppCompatActivity() {

    private var notePosition: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_note)

        val nuevoTitulo = findViewById<EditText>(R.id.nuevoTitulo)
        val guardarNota = findViewById<Button>(R.id.guardarNota)
        val nuevaDescripcion = findViewById<EditText>(R.id.nuevaDescripcion)

        val currentTitle = intent.getStringExtra("current_title")
        val currentDescription = intent.getStringExtra("current_description")
        notePosition = intent.getIntExtra("note_position", -1)

        if (currentTitle != null && currentDescription != null) {
            nuevoTitulo.setText(currentTitle)
            nuevaDescripcion.setText(currentDescription)
            guardarNota.text = "Guardar Cambios" // Cambia el texto del botón
        }

        // 1. Crea una instancia de Markwon usando su constructor (builder)
        val markwon = Markwon.builder(this)
            // 2. Añade el plugin de imágenes, que usará Coil para cargar las fotos
            .usePlugin(CoilImagesPlugin.create(this))
            .build()

        // 3. Crea el editor de Markwon
        val editor = MarkwonEditor.create(markwon)

        // 4. Asigna el TextWatcher al EditText
        nuevaDescripcion.addTextChangedListener(
            MarkwonEditorTextWatcher.withProcess(editor)
        )
        guardarNota.setOnClickListener {
            val noteTitle = nuevoTitulo.text.toString()
            val noteDescription = nuevaDescripcion.text.toString()

            if (noteTitle.isNotEmpty()) {
                val resultIntent = Intent()
                resultIntent.putExtra("note_title", noteTitle)
                resultIntent.putExtra("note_description", noteDescription)
                setResult(RESULT_OK, resultIntent)
                finish()
            }
        }
    }
}

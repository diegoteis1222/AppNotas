package com.example.notas

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.mukesh.MarkdownView
import io.noties.markwon.Markwon

class NoteDetailActivity : AppCompatActivity() {

    // Es un objeto singleton asociado a una clase. Solo puede haber uno por clase.
    // Sus miembros pueden ser invocados directamente usando el nombre de la clase.
    companion object {
        const val EXTRA_NOTE_TITULO = "extra_note_titulo"
        const val EXTRA_NOTE_DESCIPCION = "extra_note_descripcion"
        const val EXTRA_NOTE_POSICION = "extra_note_posicion"
    }

    //lateint significa que vas a inicializarlo mas tarde.
    private lateinit var textViewTitulo: TextView
    private lateinit var textViewDescripcion: TextView
    private lateinit var markwon: Markwon
    private var notePosition: Int = -1

    // Lanzador para la actividad de edición
    private val editLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val newTitle = data?.getStringExtra("note_title")
                val newDescription = data?.getStringExtra("note_description")

                if (newTitle != null && newDescription != null) {
                    // 1. Actualiza la UI de esta actividad al instante
                    textViewTitulo.text = newTitle
                    markwon.setMarkdown(textViewDescripcion, newDescription)

                    // 2. Prepara el resultado para devolverlo a MainActivity
                    val resultIntent = Intent().apply {
                        putExtra("note_title", newTitle)
                        putExtra("note_description", newDescription)
                        putExtra("note_position", notePosition)
                    }
                    setResult(Activity.RESULT_OK, resultIntent)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_detail) //le dice que layout usar

        val markdownView = findViewById<MarkdownView>(R.id.markdown_view)
        val tuTextoMarkdown = "Este es un ejemplo de texto con una imagen: ![Logo de Google](https://www.google.com/images/branding/googlelogo/1x/googlelogo_color_272x92dp.png)"
        markdownView.setMarkDownText(tuTextoMarkdown)

        // Inicializacion de Markwon para renderizar Markdown
        markwon = Markwon.create(this)

        //finders
        textViewTitulo = findViewById(R.id.detalleTitulo)
        textViewDescripcion = findViewById(R.id.detalleDescripcion)
        val botonEditar: Button = findViewById(R.id.botonEditar)

        // Recibe los datos de MainActivity usando el intent
        val titulo = intent.getStringExtra(EXTRA_NOTE_TITULO)
        val descripcion = intent.getStringExtra(EXTRA_NOTE_DESCIPCION)
        notePosition = intent.getIntExtra(EXTRA_NOTE_POSICION, -1)

        // Muestra los datos iniciales
        textViewTitulo.text = titulo
        markwon.setMarkdown(textViewDescripcion, descripcion ?: "")

        // logica para editar la nota
        botonEditar.setOnClickListener {
            // Lanza AddNoteActivity en modo "edición"
            val intent = Intent(this, AddNoteActivity::class.java).apply {
                putExtra("current_title", textViewTitulo.text.toString())
                putExtra("current_description", descripcion)
                putExtra("note_position", notePosition)
            }
            editLauncher.launch(intent)
        }
    }
}
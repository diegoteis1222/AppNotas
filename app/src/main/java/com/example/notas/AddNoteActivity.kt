package com.example.notas

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
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
        val errorTextView = findViewById<TextView>(R.id.errorTextView)

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
            val noteTitle = nuevoTitulo.text.toString().trim()
            val noteDescription = nuevaDescripcion.text.toString().trim()

            // --- LÓGICA DE VALIDACIÓN ---

            // 1. Siempre oculta el error al empezar
            errorTextView.visibility = View.GONE

            // 2. Comprueba el título
            if (noteTitle.isEmpty()) {
                errorTextView.text = "Debes añadir un título"

                // --- ANIMACIÓN DE APARICIÓN ---
                val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
                errorTextView.startAnimation(fadeIn)
                errorTextView.visibility = View.VISIBLE

                // --- ANIMACIÓN DE DESAPARICIÓN ---
                Handler(Looper.getMainLooper()).postDelayed({
                    val fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)
                    errorTextView.startAnimation(fadeOut)

                    // Importante: Oculta la vista DESPUÉS de que la animación termine
                    fadeOut.setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationStart(animation: Animation?) {}
                        override fun onAnimationEnd(animation: Animation?) {
                            errorTextView.visibility = View.GONE
                        }
                        override fun onAnimationRepeat(animation: Animation?) {}
                    })
                }, 3000)

                return@setOnClickListener
            }

            val resultIntent = Intent()
            resultIntent.putExtra("note_title", noteTitle)
            resultIntent.putExtra("note_description", noteDescription)

            if (notePosition != -1) {
                resultIntent.putExtra("note_position", notePosition)
            }

            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }
}

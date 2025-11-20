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
        setContentView(R.layout.activity_add_note) //le dice que layout usar

        //Finders
        val nuevoTitulo = findViewById<EditText>(R.id.nuevoTitulo)
        val guardarNota = findViewById<Button>(R.id.guardarNota)
        val nuevaDescripcion = findViewById<EditText>(R.id.nuevaDescripcion)
        val errorTextView = findViewById<TextView>(R.id.errorTextView)

        // Recibe los valores desde NoteDetailActivity
        val currentTitle = intent.getStringExtra("current_title")
        val currentDescription = intent.getStringExtra("current_description")
        notePosition = intent.getIntExtra("note_position", -1)

        // Si no son null significa que ya existe por lo que es una edicion
        if (currentTitle != null && currentDescription != null) {
            nuevoTitulo.setText(currentTitle)
            nuevaDescripcion.setText(currentDescription)
            guardarNota.text = "Guardar Cambios" // Cambia el texto del botón
        }

        guardarNota.setOnClickListener {
            val noteTitle = nuevoTitulo.text.toString().trim()
            val noteDescription = nuevaDescripcion.text.toString().trim()

            // --- LOGICA DE VALIDACIÓN ---

            // El error permace oculto de base
            errorTextView.visibility = View.GONE

            // Comprueba si escribiste algo en titulo
            if (noteTitle.isEmpty()) {
                errorTextView.text = "Debes añadir un título"

                // Animacion de aparicion
                val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in) // carga la animación de aparicion
                errorTextView.startAnimation(fadeIn) // ejecuta la animación
                errorTextView.visibility = View.VISIBLE // pone el error visible

                // Animacion de desaparicion
                Handler(Looper.getMainLooper()).postDelayed({
                    val fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out) // carga la animación de desaparicion
                    errorTextView.startAnimation(fadeOut) // ejecuta la animación

                    // OcultaR la vista despues de que la animación termine
                    fadeOut.setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationStart(animation: Animation?) {}
                        override fun onAnimationEnd(animation: Animation?) {
                            errorTextView.visibility = View.GONE // oculta el error
                        }
                        override fun onAnimationRepeat(animation: Animation?) {}
                    })
                }, 3000) // a los 3 segundos desaparece

                return@setOnClickListener
            }

            // --- FIN LOGICA DE VALIDACIÓN ---

            // Se crea un Intent vacio
            val resultIntent = Intent()
            // Se añaden los datos a enviar
            resultIntent.putExtra("note_title", noteTitle)
            resultIntent.putExtra("note_description", noteDescription)

            // Comprueba si estamos en modo edicion
            if (notePosition != -1) {
                resultIntent.putExtra("note_position", notePosition)
            }

            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }
}

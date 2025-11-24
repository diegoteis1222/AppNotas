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
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AddNoteActivity : AppCompatActivity() {

    // --- CONSTANTES ---
    companion object {
        const val EXTRA_TITLE = "note_title"
        const val EXTRA_DESCRIPTION = "note_description"
        const val EXTRA_POSITION = "note_position"

        const val INPUT_CURRENT_TITLE = "current_title"
        const val INPUT_CURRENT_DESCRIPTION = "current_description"
    }

    // --- VARIABLES GLOBALES DE LA CLASE ---
    // Declaramos las vistas aquí para poder usarlas en cualquier función de la clase.
    private lateinit var etTitulo: EditText
    private lateinit var etDescripcion: EditText
    private lateinit var btnGuardar: Button
    private lateinit var tvError: TextView

    // Variable para rastrear si estamos editando una nota existente (-1 significa nueva nota)
    private var notePosition: Int = -1

    // --- OnCreate ---
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_note)

        finders()

        // 2. Comprobar si llegamos a esta pantalla para editar una nota existente
        verificarModoEdicion()

        // 3. Configurar las acciones de los botones
        configurarListeners()
    }// --- Fin de onCreate ---

    // --- Métodos de Configuración y Inicialización ---
    /**
     * Vincula las variables de la clase con sus vistas correspondientes en el layout XML
     */
    private fun finders() {
        etTitulo = findViewById(R.id.nuevoTitulo)
        etDescripcion = findViewById(R.id.nuevaDescripcion)
        btnGuardar = findViewById(R.id.guardarNota)
        tvError = findViewById(R.id.errorTextView)
    }

    /**
     * Verifica si se han pasado datos a través del Intent.
     * Si hay datos, rellena los campos y cambia el texto del botón.
     */
    private fun verificarModoEdicion() {
        val currentTitle = intent.getStringExtra(INPUT_CURRENT_TITLE)
        val currentDescription = intent.getStringExtra(INPUT_CURRENT_DESCRIPTION)
        notePosition = intent.getIntExtra(EXTRA_POSITION, -1)

        // Si los datos no son nulos, estamos en modo EDICIÓN
        if (currentTitle != null && currentDescription != null) {
            etTitulo.setText(currentTitle)
            etDescripcion.setText(currentDescription)
            btnGuardar.text = "Guardar Cambios"
        }
    }

    /**
     * Configura los eventos de clic (Listeners).
     */
    private fun configurarListeners() {
        btnGuardar.setOnClickListener {
            procesarGuardado()
        }
    }
    // --- Fin de: Métodos de Configuración y Inicialización ---


    // --- Lógica de validaciones y guardado ---

    /**
     * Valida los datos y, si son correctos, prepara el resultado para devolverlo.
     */
    private fun procesarGuardado() {
        val noteTitle = etTitulo.text.toString().trim()
        val noteDescription = etDescripcion.text.toString().trim()

        // Reiniciar estado del error (ocultarlo)
        tvError.visibility = View.GONE

        // Validación: El título es obligatorio
        if (noteTitle.isEmpty()) {
            mostrarAnimacionError("Debes añadir un título")
            return
        }

        // Si pasa la validación, preparamos el Intent de retorno
        val resultIntent = Intent().apply {
            putExtra(EXTRA_TITLE, noteTitle)
            putExtra(EXTRA_DESCRIPTION, noteDescription)

            // Si es una edición, devolvemos también la posición original
            if (notePosition != -1) {
                putExtra(EXTRA_POSITION, notePosition)
            }
        }

        // Finalizamos la actividad devolviendo OK
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
    // --- Fin de: Lógica de validaciones y guardado ---

    // --- FUNCIONES VISUALES / ANIMACIONES ---

    /**
     * Muestra el mensaje de error con una animación de entrada y salida automática.
     * @param mensaje El texto a mostrar en el error.
     */
    private fun mostrarAnimacionError(mensaje: String) {
        tvError.text = mensaje

        // 1. Animación de Entrada (Fade In)
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        tvError.startAnimation(fadeIn)
        tvError.visibility = View.VISIBLE

        // 2. Programar la salida después de 3 segundos
        Handler(Looper.getMainLooper()).postDelayed({
            // Verificar si la actividad sigue válida para evitar crashes si el usuario salió
            if (!isFinishing) {
                val fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)
                tvError.startAnimation(fadeOut)

                // Listener para ocultar la vista (GONE) exactamente cuando termine la animación
                fadeOut.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {}

                    override fun onAnimationEnd(animation: Animation?) {
                        tvError.visibility = View.GONE
                    }

                    override fun onAnimationRepeat(animation: Animation?) {}
                })
            }
        }, 3000)
    }
    // ---Fin de: FUNCIONES VISUALES / ANIMACIONES ---

}
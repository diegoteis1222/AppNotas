package com.example.notas

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class ConfiguracionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configuracion)

        // Habilitar botón de atrás en la barra superior
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Configuración"

        val btnBorrarTodo = findViewById<Button>(R.id.btn_borrar_todo)

        btnBorrarTodo.setOnClickListener {
            mostrarDialogoConfirmacion()
        }
    }

    // Al pulsar la flecha de atrás en la barra superior
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun mostrarDialogoConfirmacion() {
        AlertDialog.Builder(this)
            .setTitle("¿Estás seguro?")
            .setMessage("Se borrarán todas las notas guardadas permanentemente.")
            .setPositiveButton("Sí, borrar") { _, _ ->
                borrarTodasLasNotas()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun borrarTodasLasNotas() {
        val prefs = getSharedPreferences("notas", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.clear() // Borra todo el contenido
        editor.apply()

        Toast.makeText(this, "Notas eliminadas correctamente", Toast.LENGTH_SHORT).show()
    }
}
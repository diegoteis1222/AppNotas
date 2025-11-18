package com.example.notas

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    // ---- Lista de cosas a hacer-----
    // todo cambiar entre tema claro y oscuro
    // todo cambiar el fondo de la descipcion para que parezca MD
    // todo arreglar el fondo negro en el mensaje de error o cambiarlo a un toast
    // todo comentar el codigo del main

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NoteAdapter
    private lateinit var notes: MutableList<Note>
    private lateinit var accionBoton: Button
    private lateinit var onBackPressedCallback: OnBackPressedCallback

    //Para el menu
    private lateinit var menuIcon: ImageView

    private lateinit var drawerLayout: androidx.drawerlayout.widget.DrawerLayout
    private lateinit var navigationView: com.google.android.material.navigation.NavigationView
    private lateinit var toggle: androidx.appcompat.app.ActionBarDrawerToggle



    // manejar el resultado de AddNoteActivity
    private val addNoteLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val noteTitle = data?.getStringExtra("note_title")
                val noteDescription = data?.getStringExtra("note_description")

                if (!noteTitle.isNullOrEmpty() && noteDescription != null) {
                    notes.add(Note(noteTitle, noteDescription))
                    adapter.notifyItemInserted(notes.size - 1)
                    saveNotes()
                }
            }
        }

    private val detailLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val noteTitle = data?.getStringExtra("note_title")
                val noteDescription = data?.getStringExtra("note_description")
                val position = data?.getIntExtra("note_position", -1) ?: -1

                if (position != -1 && noteTitle != null && noteDescription != null) {
                    // Actualiza la nota en la lista
                    notes[position] = Note(noteTitle, noteDescription)
                    // Notifica al adaptador del cambio específico
                    adapter.notifyItemChanged(position)
                    // Guarda los cambios
                    saveNotes()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Código del menú
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)

        toggle = androidx.appcompat.app.ActionBarDrawerToggle(
            this,
            drawerLayout,
            R.string.open,
            R.string.close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_notas -> {
                    drawerLayout.closeDrawers()
                }

                R.id.nav_configuracion -> {
                    // Lógica para ir a la pantalla de configuración
                    drawerLayout.closeDrawers()
                }

                R.id.nav_acerca_de -> {
                    // Lógica para ir a la pantalla "Acerca de"
                    drawerLayout.closeDrawers()
                }
            }
            true
        } // Fin código del menú

        onBackPressedCallback = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                // Esta es la lógica que se ejecutará cuando el callback esté habilitado
                // y el usuario presione "Atrás".
                exitSelectionMode()
            }
        }
        // 2. Añadir el callback al dispatcher.
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        recyclerView = findViewById(R.id.recyclerView)
        accionBoton = findViewById(R.id.accionBoton)
        menuIcon = findViewById(R.id.menu_icon)

        // Configurar el listener para el icono del menú
        menuIcon.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // --- Logica del menu ---
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Comprobar si el menú lateral está abierto
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    // Si está abierto, cerrarlo
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    // Si el menú está cerrado, realizar la acción de retroceso por defecto.
                    // Para ello, desactivamos temporalmente este callback y volvemos a llamar al dispatcher.
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        }

        // Añadir el callback al dispatcher
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        // --- Fin logica del menu ---

        notes = loadNotes()
        adapter = NoteAdapter(notes, { note, position ->
            // Si estamos en modo de selección, el clic normal alterna la selección
            if (adapter.selectionMode) {
                adapter.toggleSelection(position)
                // Oculta el botón de borrar si no queda nada seleccionado
                if (adapter.getSelectedNotes().isEmpty()) {
                    exitSelectionMode()
                }
            } else {
                // Envia a traves del intent los datos de la nota a NotaDetalActivity
                val intent = Intent(this, NoteDetailActivity::class.java).apply {
                    putExtra(NoteDetailActivity.EXTRA_NOTE_TITULO, note.text)
                    putExtra(NoteDetailActivity.EXTRA_NOTE_DESCIPCION, note.description)
                    putExtra(NoteDetailActivity.EXTRA_NOTE_POSICION, position)
                }
                detailLauncher.launch(intent)
            }
        }, { position ->
            // El clic largo inicia el modo de selección
            if (!adapter.selectionMode) {
                adapter.startSelectionMode(position)
                enterSelectionMode()
            }

        })

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // La configuración inicial del listener se moverá a una función
        // para poder restaurarla después.
        setAddNoteListener()

    }

    private fun setAddNoteListener() {
        accionBoton.setOnClickListener {
            val intent = Intent(this, AddNoteActivity::class.java)
            addNoteLauncher.launch(intent)
        }
    }

    private fun setDeleteNotesListener() {
        accionBoton.setOnClickListener {
            val selectedNotes = adapter.getSelectedNotes()
            notes.removeAll(selectedNotes.toSet())
            saveNotes()
            exitSelectionMode()
            adapter.notifyDataSetChanged()
        }
    }

    private fun enterSelectionMode() {
        // Transformamos el botón para que sea "Borrar"
        accionBoton.text = "Borrar seleccionados"
        // Opcional: Cambiamos el color para que sea más visual
        accionBoton.setBackgroundColor(
            ContextCompat.getColor(
                this,
                R.color.my_red_error
            )
        )
        // Cambiamos la acción que ejecutará el botón
        setDeleteNotesListener()

        // Habilitamos el callback para el botón "atrás"
        onBackPressedCallback.isEnabled = true
    }

    private fun exitSelectionMode() {
        adapter.clearSelection()

        // Restauramos el botón a su estado original "Agregar Nota"
        accionBoton.text = "Agregar nota"
        // Restauramos el color original
        accionBoton.setBackgroundColor(
            ContextCompat.getColor(
                this,
                R.color.my_blue_primary
            )
        )

        // Restauramos la acción original del botón
        setAddNoteListener()

        // Deshabilitamos el callback
        onBackPressedCallback.isEnabled = false
    }

    private fun saveNotes() {
        val prefs = getSharedPreferences("notas", MODE_PRIVATE)
        val editor = prefs.edit()
        // Guardamos título y descripción separados por ":::" y las notas por "|||"
        val noteStrings = notes.joinToString("|||") { "${it.text}:::${it.description}" }
        editor.putString("lista", noteStrings)
        editor.apply()
    }

    private fun loadNotes(): MutableList<Note> {
        val prefs = getSharedPreferences("notas", MODE_PRIVATE)
        val data = prefs.getString("lista", null) ?: return mutableListOf()
        if (data.isEmpty()) return mutableListOf()

        return data.split("|||").mapNotNull { noteString ->
            val parts = noteString.split(":::")
            if (parts.size == 2) {
                Note(text = parts[0], description = parts[1])
            } else {
                null // Ignora las notas con formato incorrecto
            }
        }.toMutableList()
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
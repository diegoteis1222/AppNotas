package com.example.notas

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView

/**
 * Actividad principal de la aplicación. Muestra la lista de notas del usuario
 * y permite crearlas, verlas, editarlas y borrarlas.
 */
class MainActivity : AppCompatActivity() {

    // --- Variables y Vistas de la Clase ---
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NoteAdapter
    private lateinit var notes: MutableList<Note>
    private lateinit var accionBoton: Button
    private lateinit var menuIcon: ImageView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var onBackPressedCallback: OnBackPressedCallback

    // --- Launchers para resultados de Actividades ---

    /**
     * Gestiona el resultado de [AddNoteActivity]. Se activa cuando el usuario crea una nueva nota.
     * Si el resultado es [Activity.RESULT_OK], extrae los datos de la nota, la añade a la lista
     * y la acrualiza.
     */
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

    /**
     * Gestiona el resultado de [NoteDetailActivity]. Se activa cuando el usuario edita una nota existente.
     * Si el resultado es [Activity.RESULT_OK], actualiza la nota en la posición correspondiente
     * de la lista y guarda los cambios.
     */
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

    // --- Fin de: Launchers para resultados de Actividades ---


    // --- OnCreate ---

    /**
     * Punto de entrada de la actividad. Se llama cuando la actividad se crea por primera vez.
     * Orquesta la inicialización de las vistas, datos, listeners y otros componentes.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Uso de los metodos
        finders()
        setupMenu()
        setupRecyclerView()
        setupListeners()
        setupOnBackPressed()
    } // --- Fin de: onCreate ---

    /**
     * Se llama cada vez que la actividad vuelve al primer plano.
     * Recargamos las notas por si se borraron desde Configuración.
     */
    override fun onResume() {
        super.onResume()
        // Recargamos la lista desde las SharedPreferences
        val listaActualizada = loadNotes()

        // Limpiamos la lista en memoria y añadimos las nuevas
        notes.clear()
        notes.addAll(listaActualizada)

        // Avisamos al adaptador
        adapter.notifyDataSetChanged()
    }

    /**
     * Maneja los clics en los elementos del menú de la barra de acciones.
     * Si el elemento es el ícono del menú, abre el menú lateral.
     */
    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    // --- Métodos de Configuración y Inicialización ---

    /**
     * Vincula las variables de la clase con sus vistas correspondientes en el layout XML
     * y carga las notas guardadas
     */
    private fun finders() {
        recyclerView = findViewById(R.id.recyclerView)
        accionBoton = findViewById(R.id.accionBoton)
        menuIcon = findViewById(R.id.menu_icon)
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)

        notes = loadNotes()
    }

    /**
     * Configura el Navigation Drawer (menú lateral), incluyendo el ActionBarDrawerToggle
     * y el listener para los clics en sus elementos.
     */
    private fun setupMenu() {
        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_notas -> {
                    // Ya estamos en notas, solo cerramos el menú
                }

                R.id.nav_configuracion -> {
                    val intent = Intent(this, ConfiguracionActivity::class.java)
                    startActivity(intent)
                }

                R.id.nav_acerca_de -> {
                    // Aquí podrías poner otra actividad o un Toast
                    // Toast.makeText(this, "App Notas v1.0", Toast.LENGTH_SHORT).show()
                }
            }
            // Cierra el menú después de cualquier selección
            drawerLayout.closeDrawers()
            true
        }
    }

    /**
     * Inicializa el [RecyclerView], su [NoteAdapter] y el [LinearLayoutManager].
     * Define las acciones a realizar ante un clic normal o un clic largo en una nota.
     */
    private fun setupRecyclerView() {
        adapter = NoteAdapter(notes, { note, position ->
            // Si estamos en modo de selección, el clic normal alterna la selección
            if (adapter.selectionMode) {
                adapter.toggleSelection(position)
                if (adapter.getSelectedNotes().isEmpty()) {
                    exitSelectionMode()
                }
            } else {
                // Lanza la actividad de detalle para ver o editar la nota
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
                enterSelectionMode(position)
            }
        })

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    /**
     * Configura los listeners iniciales para las vistas interactivas, como el botón de acción
     * principal y el ícono del menú.
     */
    private fun setupListeners() {
        // Configurar el listener para el icono del menú
        menuIcon.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // La configuración inicial del listener es para añadir una nota
        setAddNoteListener()
    }

    /**
     * Configura el comportamiento del botón de retroceso del sistema.
     * Gestiona el cierre del menú lateral y la salida del modo de selección
     * antes de ejecutar la acción de retroceso por defecto.
     */
    private fun setupOnBackPressed() {
        onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when {
                    drawerLayout.isDrawerOpen(GravityCompat.START) -> drawerLayout.closeDrawer(
                        GravityCompat.START
                    )

                    adapter.selectionMode -> exitSelectionMode()
                    else -> {
                        // Desactivamos temporalmente para evitar un bucle y llamamos a la acción original
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                        isEnabled = true
                    }
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    // --- Fin de: Métodos de Configuración y Inicialización ---

    // --- Lógica del Modo de Selección ---

    /**
     * Activa el modo de selección múltiple en la UI.
     * Cambia la apariencia y la funcionalidad del botón de acción para permitir el borrado.
     * @param position La posición inicial del elemento que activó el modo de selección.
     */
    private fun enterSelectionMode(position: Int) {
        adapter.startSelectionMode(position)
        setDeleteNotesListener()
    }

    /**
     * Desactiva el modo de selección múltiple.
     * Limpia la selección en el adaptador y restaura el botón de acción a su estado original.
     */
    private fun exitSelectionMode() {
        adapter.clearSelection()
        setAddNoteListener()
    }

    /**
     * Configura el botón de acción para que su funcionalidad sea la de añadir una nueva nota.
     */
    private fun setAddNoteListener() {
        accionBoton.text = "Agregar nota"
        accionBoton.setBackgroundColor(ContextCompat.getColor(this, R.color.my_blue_primary))
        accionBoton.setOnClickListener {
            val intent = Intent(this, AddNoteActivity::class.java)
            addNoteLauncher.launch(intent)
        }
    }

    /**
     * Configura el botón de acción para que su funcionalidad sea la de borrar las notas seleccionadas.
     */
    private fun setDeleteNotesListener() {
        accionBoton.text = "Borrar seleccionados"
        accionBoton.setBackgroundColor(ContextCompat.getColor(this, R.color.my_red_error))
        accionBoton.setOnClickListener {
            val selectedNotes = adapter.getSelectedNotes()
            notes.removeAll(selectedNotes.toSet())
            saveNotes()
            exitSelectionMode()
            adapter.notifyDataSetChanged()
        }
    }
    // --- Fin de: Lógica del Modo de Selección ---


    // --- Lógica de guardado y cargado de datos ---

    /**
     * Guarda la lista actual de notas en [SharedPreferences].
     * Las notas se serializan a un único String usando separadores personalizados.
     */
    private fun saveNotes() {
        val prefs = getSharedPreferences("notas", MODE_PRIVATE)
        val editor = prefs.edit()
        // Guardamos título y descripción separados por ":::" y las notas por "|||"
        val noteStrings = notes.joinToString("|||") { "${it.text}:::${it.description}" }
        editor.putString("lista", noteStrings)
        editor.apply()
    }

    /**
     * Carga la lista de notas desde [SharedPreferences].
     * Si no hay datos guardados o los datos están vacíos, devuelve una lista mutable vacía.
     * @return Una [MutableList] de objetos [Note].
     */
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

    // --- Fin de: Lógica de guardado y cargado de datos ---

}
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var db: NoteDatabase
    private lateinit var adapter: NoteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize database with error handling
        try {
            db = NoteDatabase.get(this)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Database error: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Create UI programmatically
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFF030a06.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            setPadding(32, 32, 32, 32)
        }

        val addBtn = Button(this).apply {
            text = "+ NEW NOTE"
            setTextColor(0xFF030a06.toInt())
            setBackgroundColor(0xFF00FF6A.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val recycler = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,  // weight
                1f  // take remaining space
            )
        }

        layout.addView(addBtn)
        layout.addView(recycler)
        setContentView(layout)

        // Setup adapter
        adapter = NoteAdapter(
            notes = emptyList(),
            onClick = { note ->
                val intent = Intent(this, AddEditNoteActivity::class.java)
                intent.putExtra("note_id", note.id)
                startActivity(intent)
            },
            onLongClick = { note ->
                AlertDialog.Builder(this)
                    .setTitle("Delete '${note.title}'?")
                    .setPositiveButton("DELETE") { _, _ ->
                        lifecycleScope.launch {
                            try {
                                db.noteDao().delete(note)
                                loadNotes()
                                Toast.makeText(this@MainActivity, "Deleted", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(this@MainActivity, "Delete failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    .setNegativeButton("CANCEL", null)
                    .show()
            }
        )
        recycler.adapter = adapter

        addBtn.setOnClickListener {
            startActivity(Intent(this, AddEditNoteActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadNotes()
    }

    private fun loadNotes() {
        lifecycleScope.launch {
            try {
                val notes = db.noteDao().getAll()
                adapter.update(notes)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@MainActivity, "Failed to load notes", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

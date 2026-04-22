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
        db = NoteDatabase.get(this)

        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setBackgroundColor(0xFF030a06.toInt())
        }

        val recycler = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
        }

        val addBtn = Button(this).apply {
            text = "+ NEW NOTE"
            setTextColor(0xFF030a06.toInt())
            setBackgroundColor(0xFF00FF6A.toInt())
        }

        adapter = NoteAdapter(
            notes = emptyList(),
            onClick = { note ->
                startActivity(
                    Intent(this, AddEditNoteActivity::class.java)
                        .putExtra("note_id", note.id)
                )
            },
            onLongClick = { note ->
                AlertDialog.Builder(this)
                    .setTitle("Delete '${note.title}'?")
                    .setPositiveButton("DELETE") { _, _ ->
                        lifecycleScope.launch {
                            db.noteDao().delete(note)
                            loadNotes()
                        }
                    }
                    .setNegativeButton("CANCEL", null)
                    .show()
            }
        )

        recycler.adapter = adapter
        layout.addView(addBtn)
        layout.addView(recycler)
        setContentView(layout)

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
            adapter.update(db.noteDao().getAll())
        }
    }
}

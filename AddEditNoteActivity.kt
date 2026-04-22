import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class AddEditNoteActivity : AppCompatActivity() {

    private lateinit var db: NoteDatabase
    private var existingNote: Note? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = NoteDatabase.get(this)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
            setBackgroundColor(0xFF030a06.toInt())
        }

        val titleInput = EditText(this).apply {
            hint = "Title"
            setHintTextColor(0xFF3a6a48.toInt())
            setTextColor(0xFF00FF6A.toInt())
            textSize = 18f
            background = null
        }

        val contentInput = EditText(this).apply {
            hint = "Note..."
            setHintTextColor(0xFF3a6a48.toInt())
            setTextColor(0xFFc8f0d0.toInt())
            textSize = 15f
            background = null
            minLines = 10
            gravity = android.view.Gravity.TOP
        }

        val saveBtn = Button(this).apply {
            text = "SAVE"
            setTextColor(0xFF030a06.toInt())
            setBackgroundColor(0xFF00FF6A.toInt())
        }

        layout.addView(titleInput)
        layout.addView(contentInput)
        layout.addView(saveBtn)
        setContentView(layout)

        // Load existing note if editing
        intent.getIntExtra("note_id", -1).let { id ->
            if (id != -1) {
                lifecycleScope.launch {
                    val all = db.noteDao().getAll()
                    existingNote = all.find { it.id == id }
                    existingNote?.let {
                        titleInput.setText(it.title)
                        contentInput.setText(it.content)
                    }
                }
            }
        }

        saveBtn.setOnClickListener {
            val title = titleInput.text.toString().trim()
            val content = contentInput.text.toString().trim()

            if (title.isEmpty()) {
                Toast.makeText(this, "Title can't be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                db.noteDao().insert(
                    Note(
                        id = existingNote?.id ?: 0,
                        title = title,
                        content = content
                    )
                )
                finish()
            }
        }
    }
}

package com.example.harness

import android.os.Bundle
import android.text.SpannedString
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.harness.databinding.ActivityFeatureBinding
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class FeatureActivity : AppCompatActivity() {
    protected lateinit var binding: ActivityFeatureBinding
    private lateinit var resultAdapter: ResultAdapter
    protected abstract val memoryAware: MemoryAware

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeatureBinding.inflate(layoutInflater)
        setContentView(binding.root)
        resultAdapter = ResultAdapter()
        binding.listResults.layoutManager = LinearLayoutManager(this)
        binding.listResults.adapter = resultAdapter
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        memoryAware.onTrimMemory(level)
    }

    protected fun configureScreen(
        title: String,
        buttonLabels: List<String>,
        showPrimary: Boolean = false,
        showSecondary: Boolean = false,
        spinnerOptions: List<String>? = null,
        showResults: Boolean = true
    ) {
        binding.toolbar.title = title
        binding.inputPrimary.visibility = if (showPrimary) View.VISIBLE else View.GONE
        binding.inputSecondary.visibility = if (showSecondary) View.VISIBLE else View.GONE
        binding.listResults.visibility = if (showResults) View.VISIBLE else View.GONE
        if (spinnerOptions == null) {
            binding.spinnerOption.visibility = View.GONE
        } else {
            binding.spinnerOption.visibility = View.VISIBLE
            binding.spinnerOption.adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                spinnerOptions
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
        }

        val buttons = listOf(
            binding.btnAction1,
            binding.btnAction2,
            binding.btnAction3,
            binding.btnAction4,
            binding.btnAction5
        )
        buttons.forEachIndexed { index, button ->
            if (index < buttonLabels.size) {
                button.text = buttonLabels[index]
                button.visibility = View.VISIBLE
            } else {
                button.visibility = View.GONE
            }
        }
    }

    protected fun actionButton(index: Int): MaterialButton = when (index) {
        1 -> binding.btnAction1
        2 -> binding.btnAction2
        3 -> binding.btnAction3
        4 -> binding.btnAction4
        5 -> binding.btnAction5
        else -> error("Unsupported action button")
    }

    protected fun <T> runFeatureCall(call: () -> T, showResult: (T) -> Unit) {
        lifecycleScope.launch {
            try {
                val result = withContext(Dispatchers.IO) { call() }
                showResult(result)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                binding.txtStatus.text =
                    exception.message ?: exception.javaClass.simpleName
            }
        }
    }

    protected fun showStatus(status: String) {
        binding.txtStatus.text = status
    }

    protected fun showRows(rows: List<Pair<String, String>>) {
        resultAdapter.submitRows(rows)
    }

    protected fun noteRows(notes: List<NoteSummary>): List<Pair<String, String>> =
        notes.map { note -> note.title to note.preview }

    protected fun selectedOption(): Int = binding.spinnerOption.selectedItemPosition
}

class NotesListActivity : FeatureActivity() {
    override val memoryAware: MemoryAware
        get() = Logic.notes

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureScreen("F01 Notes", listOf("Load", "Add"), showPrimary = true, showSecondary = true)
        binding.inputPrimary.hint = "Title"
        binding.inputSecondary.hint = "Body"
        binding.inputSecondary.minLines = 3

        actionButton(1).setOnClickListener {
            runFeatureCall({ Logic.notes.loadNotes() }) { notes ->
                showRows(noteRows(notes))
                showStatus("items: ${notes.size}")
            }
        }
        actionButton(2).setOnClickListener {
            val title = binding.inputPrimary.text.toString()
            val body = binding.inputSecondary.text.toString()
            runFeatureCall({ Logic.notes.saveNote(title, body) }) { id ->
                showStatus("id: $id")
            }
        }
    }
}

class RichTextActivity : FeatureActivity() {
    override val memoryAware: MemoryAware
        get() = Logic.richText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureScreen(
            "F02 Rich text",
            listOf("Bold", "Italic", "Mono", "Round-trip"),
            showPrimary = true,
            showResults = false
        )
        binding.inputPrimary.hint = "Text"
        binding.inputPrimary.minLines = 5

        fun apply(style: TextStyleType) {
            val text = SpannedString.valueOf(binding.inputPrimary.text)
            val start = binding.inputPrimary.selectionStart
            val end = binding.inputPrimary.selectionEnd
            if (start < 0 || end < 0) {
                showStatus("Select text first")
                return
            }
            runFeatureCall({ Logic.richText.applyStyle(text, start, end, style) }) { styled ->
                binding.inputPrimary.setText(styled)
                showStatus("ok")
            }
        }

        actionButton(1).setOnClickListener { apply(TextStyleType.BOLD) }
        actionButton(2).setOnClickListener { apply(TextStyleType.ITALIC) }
        actionButton(3).setOnClickListener { apply(TextStyleType.MONOSPACE) }
        actionButton(4).setOnClickListener {
            val text = SpannedString.valueOf(binding.inputPrimary.text)
            runFeatureCall({
                Logic.richText.deserialize(Logic.richText.serialize(text))
            }) { restored ->
                binding.inputPrimary.setText(restored)
                showStatus("ok")
            }
        }
    }
}

class AttachmentsActivity : FeatureActivity() {
    override val memoryAware: MemoryAware
        get() = Logic.attachments

    private lateinit var attachPicker: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        attachPicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                runFeatureCall({ Logic.attachments.attach(1L, uri) }) { attachment ->
                    showRows(listOf(attachment.displayName to "${attachment.sizeBytes} bytes"))
                    showStatus("ok")
                }
            }
        }
        configureScreen("F03 Attachments", listOf("Attach", "List", "Remove first"))
        actionButton(1).setOnClickListener { attachPicker.launch("*/*") }
        actionButton(2).setOnClickListener {
            runFeatureCall({ Logic.attachments.list(1L) }) { attachments ->
                showRows(attachments.map { it.displayName to "${it.sizeBytes} bytes" })
                showStatus("items: ${attachments.size}")
            }
        }
        actionButton(3).setOnClickListener {
            runFeatureCall({ Logic.attachments.list(1L) }) { attachments ->
                if (attachments.isEmpty()) {
                    showStatus("items: 0")
                } else {
                    runFeatureCall({ Logic.attachments.remove(attachments.first().id) }) { removed ->
                        showStatus(removed.toString())
                    }
                }
            }
        }
    }
}

class SearchActivity : FeatureActivity() {
    override val memoryAware: MemoryAware
        get() = Logic.search

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureScreen("F04 Search", listOf("Search"), showPrimary = true)
        binding.inputPrimary.hint = "Query"
        actionButton(1).setOnClickListener {
            val query = binding.inputPrimary.text.toString()
            runFeatureCall({ Logic.search.search(query) }) { notes ->
                showRows(noteRows(notes))
                showStatus("items: ${notes.size}")
            }
        }
    }
}

class OrganizeActivity : FeatureActivity() {
    override val memoryAware: MemoryAware
        get() = Logic.organizer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureScreen(
            "F05 Organize",
            listOf("Set label", "Set color", "Pin", "List by label"),
            showPrimary = true,
            showSecondary = true
        )
        binding.inputPrimary.hint = "Note ID"
        binding.inputPrimary.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        binding.inputSecondary.hint = "Label"
        actionButton(1).setOnClickListener {
            val noteId = binding.inputPrimary.text.toString()
            val label = binding.inputSecondary.text.toString()
            runFeatureCall({ Logic.organizer.setLabel(noteId.toLong(), label) }) {
                showStatus(it.toString())
            }
        }
        actionButton(2).setOnClickListener {
            val noteId = binding.inputPrimary.text.toString()
            runFeatureCall({ Logic.organizer.setColor(noteId.toLong(), 0xFFFFC107.toInt()) }) {
                showStatus(it.toString())
            }
        }
        actionButton(3).setOnClickListener {
            val noteId = binding.inputPrimary.text.toString()
            runFeatureCall({ Logic.organizer.setPinned(noteId.toLong(), true) }) {
                showStatus(it.toString())
            }
        }
        actionButton(4).setOnClickListener {
            val label = binding.inputSecondary.text.toString()
            runFeatureCall({ Logic.organizer.listByLabel(label) }) { notes ->
                showRows(noteRows(notes))
                showStatus("items: ${notes.size}")
            }
        }
    }
}

class StateActivity : FeatureActivity() {
    override val memoryAware: MemoryAware
        get() = Logic.stateManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureScreen(
            "F06 State",
            listOf("Archive", "Trash", "Restore", "List state", "Empty trash"),
            showPrimary = true,
            spinnerOptions = NoteState.entries.map { it.name }
        )
        binding.inputPrimary.hint = "Note ID"
        binding.inputPrimary.inputType = android.text.InputType.TYPE_CLASS_NUMBER

        actionButton(1).setOnClickListener {
            val id = binding.inputPrimary.text.toString()
            runFeatureCall({ Logic.stateManager.archive(id.toLong()) }) { showStatus(it.toString()) }
        }
        actionButton(2).setOnClickListener {
            val id = binding.inputPrimary.text.toString()
            runFeatureCall({ Logic.stateManager.trash(id.toLong()) }) { showStatus(it.toString()) }
        }
        actionButton(3).setOnClickListener {
            val id = binding.inputPrimary.text.toString()
            runFeatureCall({ Logic.stateManager.restore(id.toLong()) }) { showStatus(it.toString()) }
        }
        actionButton(4).setOnClickListener {
            val state = NoteState.entries[selectedOption()]
            runFeatureCall({ Logic.stateManager.listByState(state) }) { notes ->
                showRows(noteRows(notes))
                showStatus("items: ${notes.size}")
            }
        }
        actionButton(5).setOnClickListener {
            runFeatureCall({ Logic.stateManager.emptyTrash() }) { showStatus("items: $it") }
        }
    }
}

class RemindersActivity : FeatureActivity() {
    override val memoryAware: MemoryAware
        get() = Logic.reminders

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureScreen(
            "F07 Reminders",
            listOf("Schedule", "Cancel", "List"),
            showPrimary = true,
            showSecondary = true
        )
        binding.inputPrimary.hint = "Note ID"
        binding.inputPrimary.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        binding.inputSecondary.hint = "Minutes from now"
        binding.inputSecondary.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        actionButton(1).setOnClickListener {
            val noteId = binding.inputPrimary.text.toString()
            val minutes = binding.inputSecondary.text.toString()
            runFeatureCall({
                Logic.reminders.schedule(
                    noteId.toLong(),
                    System.currentTimeMillis() + minutes.toLong() * 60_000L
                )
            }) {
                showStatus(it.toString())
            }
        }
        actionButton(2).setOnClickListener {
            val noteId = binding.inputPrimary.text.toString()
            runFeatureCall({ Logic.reminders.cancel(noteId.toLong()) }) { showStatus(it.toString()) }
        }
        actionButton(3).setOnClickListener {
            runFeatureCall({ Logic.reminders.listScheduled() }) { reminders ->
                showRows(reminders.map { "Note ${it.noteId}" to it.triggerAtMillis.toString() })
                showStatus("items: ${reminders.size}")
            }
        }
    }
}

class BackupActivity : FeatureActivity() {
    override val memoryAware: MemoryAware
        get() = Logic.backup

    private lateinit var exportPicker: ActivityResultLauncher<android.content.Intent>
    private lateinit var importPicker: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exportPicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val uri = result.data?.data
            if (uri != null) {
                val format = ExportFormat.entries[selectedOption()]
                runFeatureCall({ Logic.backup.exportAll(uri, format) }) { result ->
                    showStatus(
                        result.message.ifEmpty {
                            if (result.success) "items: ${result.itemCount}" else "error"
                        }
                    )
                }
            }
        }
        configureScreen(
            "F08 Backup",
            listOf("Export", "Import"),
            spinnerOptions = ExportFormat.entries.map { it.name },
            showResults = false
        )
        actionButton(1).setOnClickListener {
            val format = ExportFormat.entries[selectedOption()]
            val mimeType = when (format) {
                ExportFormat.JSON -> "application/json"
                ExportFormat.TXT -> "text/plain"
                ExportFormat.HTML -> "text/html"
            }
            val extension = when (format) {
                ExportFormat.JSON -> "json"
                ExportFormat.TXT -> "txt"
                ExportFormat.HTML -> "html"
            }
            val intent = android.content.Intent(android.content.Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(android.content.Intent.CATEGORY_OPENABLE)
                type = mimeType
                putExtra(android.content.Intent.EXTRA_TITLE, "notes.$extension")
            }
            exportPicker.launch(intent)
        }
        actionButton(2).setOnClickListener { importPicker.launch(arrayOf("*/*")) }
    }
}

class SettingsActivity : FeatureActivity() {
    override val memoryAware: MemoryAware
        get() = Logic.settings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureScreen(
            "F09 Settings",
            listOf("Save", "Load"),
            showPrimary = true,
            spinnerOptions = ThemeMode.entries.map { it.name },
            showResults = false
        )
        binding.inputPrimary.hint = "Text size (sp)"
        binding.inputPrimary.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        actionButton(1).setOnClickListener {
            val textSize = binding.inputPrimary.text.toString()
            val themeMode = ThemeMode.entries[selectedOption()]
            runFeatureCall({
                Logic.settings.save(
                    AppSettings(textSize.toInt(), themeMode, false, SortOrder.NEWEST)
                )
            }) { showStatus("ok") }
        }
        actionButton(2).setOnClickListener {
            runFeatureCall({ Logic.settings.load() }) { settings ->
                showStatus(
                    "text size: ${settings.textSizeSp}, theme: ${settings.themeMode}, " +
                        "auto backup: ${settings.autoBackup}, sort: ${settings.sortOrder}"
                )
            }
        }
    }
}

class SyncActivity : FeatureActivity() {
    override val memoryAware: MemoryAware
        get() = Logic.sync

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureScreen(
            "F10 Sync",
            listOf("Sync now"),
            showPrimary = true,
            showResults = false
        )
        binding.inputPrimary.hint = "Base URL"
        binding.inputPrimary.setText("http://127.0.0.1:8080/dav/")
        binding.inputPrimary.inputType = android.text.InputType.TYPE_CLASS_TEXT or
            android.text.InputType.TYPE_TEXT_VARIATION_URI
        actionButton(1).setOnClickListener {
            val baseUrl = binding.inputPrimary.text.toString()
            runFeatureCall({ Logic.sync.sync(baseUrl) }) { result ->
                showStatus(
                    result.error ?: "pulled: ${result.pulled}, pushed: ${result.pushed}"
                )
            }
        }
    }
}

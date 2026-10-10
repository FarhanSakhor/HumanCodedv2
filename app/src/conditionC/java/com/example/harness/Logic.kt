package com.example.harness

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri

object Logic {
    fun init(appContext: Context) {}

    val notes: NotesRepository = NotesRepositoryStub()
    val richText: RichTextEngine = RichTextEngineStub()
    val attachments: AttachmentManager = AttachmentManagerStub()
    val search: NoteSearch = NoteSearchStub()
    val organizer: NoteOrganizer = NoteOrganizerStub()
    val stateManager: NoteStateManager = NoteStateManagerStub()
    val reminders: ReminderScheduler = ReminderSchedulerStub()
    val backup: NotesBackup = NotesBackupStub()
    val settings: SettingsStore = SettingsStoreStub()
    val sync: SyncClient = SyncClientStub()
}

class NotesRepositoryStub : NotesRepository {
    override fun onTrimMemory(level: Int) {}
    override fun loadNotes(): List<NoteSummary> = emptyList()
    override fun saveNote(title: String, body: String): Long = 0L
}

class RichTextEngineStub : RichTextEngine {
    override fun onTrimMemory(level: Int) {}
    override fun applyStyle(text: CharSequence, start: Int, end: Int, style: TextStyleType): CharSequence = ""
    override fun serialize(text: CharSequence): String = ""
    override fun deserialize(stored: String): CharSequence = ""
}

class AttachmentManagerStub : AttachmentManager {
    override fun onTrimMemory(level: Int) {}
    override fun attach(noteId: Long, source: Uri): AttachmentInfo = AttachmentInfo(0L, 0L, "", 0L)
    override fun list(noteId: Long): List<AttachmentInfo> = emptyList()
    override fun remove(attachmentId: Long): Boolean = false
}

class NoteSearchStub : NoteSearch {
    override fun onTrimMemory(level: Int) {}
    override fun search(query: String): List<NoteSummary> = emptyList()
}

class NoteOrganizerStub : NoteOrganizer {
    override fun onTrimMemory(level: Int) {}
    override fun setLabel(noteId: Long, label: String): Boolean = false
    override fun setColor(noteId: Long, colorArgb: Int): Boolean = false
    override fun setPinned(noteId: Long, pinned: Boolean): Boolean = false
    override fun listByLabel(label: String): List<NoteSummary> = emptyList()
}

class NoteStateManagerStub : NoteStateManager {
    override fun onTrimMemory(level: Int) {}
    override fun archive(noteId: Long): Boolean = false
    override fun trash(noteId: Long): Boolean = false
    override fun restore(noteId: Long): Boolean = false
    override fun listByState(state: NoteState): List<NoteSummary> = emptyList()
    override fun emptyTrash(): Int = 0
}

class ReminderSchedulerStub : ReminderScheduler {
    override fun onTrimMemory(level: Int) {}
    override fun schedule(noteId: Long, triggerAtMillis: Long): Boolean = false
    override fun cancel(noteId: Long): Boolean = false
    override fun listScheduled(): List<ReminderInfo> = emptyList()
}

class NotesBackupStub : NotesBackup {
    override fun onTrimMemory(level: Int) {}
    override fun exportAll(target: Uri, format: ExportFormat): ExportResult = ExportResult(false, 0, "")
    override fun importBackup(source: Uri): Int = 0
}

class SettingsStoreStub : SettingsStore {
    override fun onTrimMemory(level: Int) {}
    override fun load(): AppSettings = AppSettings(16, ThemeMode.SYSTEM, false, SortOrder.NEWEST)
    override fun save(settings: AppSettings) {}
}

class SyncClientStub : SyncClient {
    override fun onTrimMemory(level: Int) {}
    override fun sync(baseUrl: String): SyncResult = SyncResult(0, 0, null)
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {}
}

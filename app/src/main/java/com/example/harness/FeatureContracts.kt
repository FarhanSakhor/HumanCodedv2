package com.example.harness

import android.net.Uri

/** Receives memory-pressure notifications from the owning Activity. */
interface MemoryAware {
    /** Accepts a system memory-pressure level. */
    fun onTrimMemory(level: Int)
}

interface NotesRepository : MemoryAware {
    /** Returns the notes available to the list screen. */
    fun loadNotes(): List<NoteSummary>

    /** Saves a note with the supplied title and body and returns its identifier. */
    fun saveNote(title: String, body: String): Long
}

interface RichTextEngine : MemoryAware {
    /** Applies the selected style to the requested text range. */
    fun applyStyle(text: CharSequence, start: Int, end: Int, style: TextStyleType): CharSequence

    /** Converts styled text to its stored representation. */
    fun serialize(text: CharSequence): String

    /** Restores styled text from its stored representation. */
    fun deserialize(stored: String): CharSequence
}

interface AttachmentManager : MemoryAware {
    /** Attaches the selected source to the requested note. */
    fun attach(noteId: Long, source: Uri): AttachmentInfo

    /** Returns attachments associated with the requested note. */
    fun list(noteId: Long): List<AttachmentInfo>

    /** Removes the attachment with the requested identifier. */
    fun remove(attachmentId: Long): Boolean
}

interface NoteSearch : MemoryAware {
    /** Returns notes matching the supplied query. */
    fun search(query: String): List<NoteSummary>
}

interface NoteOrganizer : MemoryAware {
    /** Assigns the supplied label to the requested note. */
    fun setLabel(noteId: Long, label: String): Boolean

    /** Assigns the supplied ARGB color to the requested note. */
    fun setColor(noteId: Long, colorArgb: Int): Boolean

    /** Sets whether the requested note is pinned. */
    fun setPinned(noteId: Long, pinned: Boolean): Boolean

    /** Returns notes assigned to the supplied label. */
    fun listByLabel(label: String): List<NoteSummary>
}

interface NoteStateManager : MemoryAware {
    /** Archives the requested note. */
    fun archive(noteId: Long): Boolean

    /** Moves the requested note to the trash. */
    fun trash(noteId: Long): Boolean

    /** Restores the requested note to the active state. */
    fun restore(noteId: Long): Boolean

    /** Returns notes in the requested state. */
    fun listByState(state: NoteState): List<NoteSummary>

    /** Permanently empties the trash and returns the number of removed notes. */
    fun emptyTrash(): Int
}

interface ReminderScheduler : MemoryAware {
    /** Schedules a reminder for the requested note and time. */
    fun schedule(noteId: Long, triggerAtMillis: Long): Boolean

    /** Cancels the reminder for the requested note. */
    fun cancel(noteId: Long): Boolean

    /** Returns all scheduled reminders. */
    fun listScheduled(): List<ReminderInfo>
}

interface NotesBackup : MemoryAware {
    /** Exports notes to the selected destination using the requested format. */
    fun exportAll(target: Uri, format: ExportFormat): ExportResult
}

interface SettingsStore : MemoryAware {
    /** Returns the current application settings. */
    fun load(): AppSettings

    /** Saves the supplied application settings. */
    fun save(settings: AppSettings)
}

interface SyncClient : MemoryAware {
    /** Synchronizes with the supplied base URL and reports the operation result. */
    fun sync(baseUrl: String): SyncResult
}

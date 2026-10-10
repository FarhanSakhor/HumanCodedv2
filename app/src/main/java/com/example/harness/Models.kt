package com.example.harness

data class NoteSummary(
    val id: Long,
    val title: String,
    val preview: String,
    val color: Int,
    val pinned: Boolean,
    val label: String?
)

data class AttachmentInfo(
    val id: Long,
    val noteId: Long,
    val displayName: String,
    val sizeBytes: Long
)

data class ReminderInfo(
    val noteId: Long,
    val triggerAtMillis: Long
)

data class ExportResult(
    val success: Boolean,
    val itemCount: Int,
    val message: String
)

data class SyncResult(
    val pulled: Int,
    val pushed: Int,
    val error: String?
)

data class AppSettings(
    val textSizeSp: Int,
    val themeMode: ThemeMode,
    val autoBackup: Boolean,
    val sortOrder: SortOrder
)

enum class NoteState {
    ACTIVE,
    ARCHIVED,
    TRASHED
}

enum class TextStyleType {
    BOLD,
    ITALIC,
    MONOSPACE
}

enum class ExportFormat {
    JSON,
    TXT,
    PDF,
    HTML
}

enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}

enum class SortOrder {
    NEWEST,
    OLDEST,
    TITLE
}

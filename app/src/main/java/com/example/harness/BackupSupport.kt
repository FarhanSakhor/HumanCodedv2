package com.example.harness

import android.content.Context
import android.net.Uri
import android.text.Editable
import android.text.Html
import androidx.core.text.toHtml
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.DateFormat

// Condition A (human) helpers for NotesBackup.
// Adapted from Notally (GPL-3.0, https://github.com/OmGodse/Notally), commit cf1b331.
// File:line references below point to that commit (viewmodels/BaseNoteModel.kt unless stated).

// Replaces preferences.showDateCreated() (lines 456 and 467).
private const val SHOW_DATE_CREATED = true

// getJSON, lines 598-619: builds the JSONObject only. The caller collects all notes into one array
// and pretty-prints it (toString(2), line 618). Spans and LIST branches dropped; color is the ARGB int.
internal fun noteToJson(note: SeedNote): JSONObject {
    return JSONObject()
        .put("type", "NOTE")
        .put("color", note.color)
        .put("title", note.title)
        .put("pinned", note.pinned)
        .put("date-created", note.timestamp)
        .put("labels", JSONArray(note.labels))
        .put("body", note.body)
}

// getTXTFile, lines 442-463: same writer pattern, looped over all notes into one file.
internal fun writeTxtFile(notes: List<SeedNote>, file: File) {
    val writer = file.bufferedWriter()

    for (note in notes) {
        val date = DateFormat.getDateInstance(DateFormat.FULL).format(note.timestamp)
        val body = note.body

        if (note.title.isNotEmpty()) {
            writer.append("${note.title}\n\n")
        }
        if (SHOW_DATE_CREATED) {
            writer.append("$date\n\n")
        }
        writer.append(body)
        writer.append("\n\n")
    }
    writer.close()
}

// getHTML, lines 621-653: one document for all notes (header and footer emitted once).
internal fun buildHtml(notes: List<SeedNote>) = buildString {
    append("<!DOCTYPE html>")
    append("<html><head>")
    append("<meta charset=\"UTF-8\"><title>Notes</title>")
    append("<style>h1{letter-spacing:0.5px}body{letter-spacing:0.25px;line-height:1.3}</style>")
    append("</head><body>")

    for (note in notes) {
        val date = DateFormat.getDateInstance(DateFormat.FULL).format(note.timestamp)
        val title = Html.escapeHtml(note.title)
        append("<h2>$title</h2>")

        if (SHOW_DATE_CREATED) {
            append("<p>$date</p>")
        }

        val body = Editable.Factory.getInstance().newEditable(note.body).toHtml()
        append(body)
    }
    append("</body></html>")
}

// writeCurrentFileToUri, lines 420-432: file and context passed in; no coroutine or Toast.
internal fun copyFileToUri(file: File, uri: Uri, context: Context) {
    val output = context.contentResolver.openOutputStream(uri) as FileOutputStream
    output.channel.truncate(0)
    val input = FileInputStream(file)
    input.copyTo(output)
    input.close()
    output.close()
}
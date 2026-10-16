package com.example.harness

import android.graphics.Typeface
import android.text.Editable
import android.text.Spannable
import android.text.Spanned
import android.text.style.CharacterStyle
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.TypefaceSpan
import android.text.style.URLSpan
import androidx.core.text.getSpans
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

// Condition A (human) helpers for RichTextEngine.
// Adapted from Notally (GPL-3.0, https://github.com/OmGodse/Notally), commit cf1b331.
// File:line references below point to that commit.

// room/SpanRepresentation.kt:3-20 (unchanged)
data class SpanRepresentation(
    var bold: Boolean,
    var link: Boolean,
    var italic: Boolean,
    var monospace: Boolean,
    var strikethrough: Boolean,
    var start: Int,
    var end: Int
) {

    fun isNotUseless(): Boolean {
        return bold || link || italic || monospace || strikethrough
    }

    fun isEqualInSize(representation: SpanRepresentation): Boolean {
        return start == representation.start && end == representation.end
    }
}

// viewmodels/NotallyModel.kt:390-415 (MarkerSpan check removed, line 393)
internal fun getFilteredSpans(spanned: Spanned): ArrayList<SpanRepresentation> {
    val representations = LinkedHashSet<SpanRepresentation>()
    spanned.getSpans<CharacterStyle>().forEach { span ->
        val end = spanned.getSpanEnd(span)
        val start = spanned.getSpanStart(span)
        val representation = SpanRepresentation(false, false, false, false, false, start, end)

        when (span) {
            is StyleSpan -> {
                representation.bold = span.style == Typeface.BOLD
                representation.italic = span.style == Typeface.ITALIC
            }

            is URLSpan -> representation.link = true
            is TypefaceSpan -> representation.monospace = span.family == "monospace"
            is StrikethroughSpan -> representation.strikethrough = true
        }

        if (representation.isNotUseless()) {
            representations.add(representation)
        }
    }
    return getFilteredRepresentations(ArrayList(representations))
}

// viewmodels/NotallyModel.kt:417-445 (unchanged)
private fun getFilteredRepresentations(representations: ArrayList<SpanRepresentation>): ArrayList<SpanRepresentation> {
    representations.forEachIndexed { index, representation ->
        val match = representations.find { spanRepresentation ->
            spanRepresentation.isEqualInSize(representation)
        }
        if (match != null && representations.indexOf(match) != index) {
            if (match.bold) {
                representation.bold = true
            }
            if (match.link) {
                representation.link = true
            }
            if (match.italic) {
                representation.italic = true
            }
            if (match.monospace) {
                representation.monospace = true
            }
            if (match.strikethrough) {
                representation.strikethrough = true
            }
            val copy = ArrayList(representations)
            copy[index] = representation
            copy.remove(match)
            return getFilteredRepresentations(copy)
        }
    }
    return representations
}

// room/Converters.kt:120-132 (unchanged)
internal fun spansToJSONArray(list: List<SpanRepresentation>): JSONArray {
    val objects = list.map { representation ->
        val jsonObject = JSONObject()
        jsonObject.put("bold", representation.bold)
        jsonObject.put("link", representation.link)
        jsonObject.put("italic", representation.italic)
        jsonObject.put("monospace", representation.monospace)
        jsonObject.put("strikethrough", representation.strikethrough)
        jsonObject.put("start", representation.start)
        jsonObject.put("end", representation.end)
    }
    return JSONArray(objects)
}

// room/Converters.kt:79-91 (takes a JSONArray instead of a String, so the array is not re-parsed)
internal fun jsonToSpans(array: JSONArray): List<SpanRepresentation> {
    val iterable = array.iterable<JSONObject>()
    return iterable.map { jsonObject ->
        val bold = jsonObject.getSafeBoolean("bold")
        val link = jsonObject.getSafeBoolean("link")
        val italic = jsonObject.getSafeBoolean("italic")
        val monospace = jsonObject.getSafeBoolean("monospace")
        val strikethrough = jsonObject.getSafeBoolean("strikethrough")
        val start = jsonObject.getInt("start")
        val end = jsonObject.getInt("end")
        SpanRepresentation(bold, link, italic, monospace, strikethrough, start, end)
    }
}

// room/Converters.kt:134-140 (unchanged)
private fun JSONObject.getSafeBoolean(name: String): Boolean {
    return try {
        getBoolean(name)
    } catch (exception: JSONException) {
        false
    }
}

// room/Converters.kt:143-157 (unchanged)
private fun <T> JSONArray.iterable() = Iterable {
    object : Iterator<T> {
        var index = 0

        override fun next(): T {
            val element = get(index)
            index++
            return element as T
        }

        override fun hasNext(): Boolean {
            return index < length()
        }
    }
}

// miscellaneous/Extensions.kt:26-51 (link branch removed, lines 36-39: the contract has no link style)
internal fun String.applySpans(representations: List<SpanRepresentation>): Editable {
    val editable = Editable.Factory.getInstance().newEditable(this)
    representations.forEach { (bold, link, italic, monospace, strikethrough, start, end) ->
        try {
            if (bold) {
                editable.setSpan(StyleSpan(Typeface.BOLD), start, end)
            }
            if (italic) {
                editable.setSpan(StyleSpan(Typeface.ITALIC), start, end)
            }
            if (monospace) {
                editable.setSpan(TypefaceSpan("monospace"), start, end)
            }
            if (strikethrough) {
                editable.setSpan(StrikethroughSpan(), start, end)
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }
    return editable
}

// miscellaneous/Extensions.kt:60-64 (unchanged)
private fun Spannable.setSpan(span: Any, start: Int, end: Int) {
    if (end <= length) {
        setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    } else setSpan(span, start, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
}
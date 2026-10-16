package com.example.harness

import java.util.Locale

/**
 * A single note in the shared starting dataset.
 * Plain data only: no behavior, no storage, no Android types.
 */
data class SeedNote(
    val id: Long,
    val title: String,
    val body: String,
    val timestamp: Long,
    val labels: List<String>,
    val color: Int,
    val pinned: Boolean
)

/**
 * Deterministic, read-only starting dataset shared by Conditions A, B and C.
 * Identical on every run and every device: no clock, no randomness.
 * Every note starts ACTIVE. Ids run from 1 to COUNT.
 * This file is shared test infrastructure and is excluded from smell analysis.
 */
object SeedData {
    const val COUNT = 500
    private const val BASE_TIMESTAMP = 1_760_000_000_000L
    private const val HOUR_MILLIS = 3_600_000L

    /** Neutral filler vocabulary. None of these words are used as search topics. */
    private val FILLER = listOf(
        "the", "and", "with", "about", "next", "last", "other", "small", "large", "early",
        "later", "again", "today", "after", "before", "notes", "item", "page", "list", "point"
    )

    /** Topic keywords. Each note carries one primary topic, so a topic matches about 10% of notes. */
    private val TOPICS = listOf(
        "meeting", "budget", "recipe", "travel", "invoice",
        "workout", "garden", "deadline", "research", "backup"
    )
    private val LABELS = listOf("work", "home", "ideas", "travel", "finance", "health")
    private val COLORS = listOf(
        0xFFFFFFFF.toInt(), 0xFFFFCDD2.toInt(), 0xFFFFE0B2.toInt(),
        0xFFFFF9C4.toInt(), 0xFFC8E6C9.toInt(), 0xFFBBDEFB.toInt()
    )

    val notes: List<SeedNote> by lazy { build() }

    private fun build(): List<SeedNote> {
        val result = ArrayList<SeedNote>(COUNT)
        for (i in 1..COUNT) {
            val topic = TOPICS[i % TOPICS.size]
            val extraTopic = if (i % 5 == 0) TOPICS[(i / 5) % TOPICS.size] else null
            val wordCount = 40 + (i * 37) % 81
            val body = buildString {
                for (j in 0 until wordCount) {
                    if (j > 0) append(' ')
                    when {
                        j == 5 -> append(topic)
                        j == 30 && extraTopic != null -> append(extraTopic)
                        else -> append(FILLER[(i * 7 + j * 13) % FILLER.size])
                    }
                    if (j % 12 == 11) append('.')
                }
            }
            result.add(
                SeedNote(
                    id = i.toLong(),
                    title = "Note %04d %s".format(Locale.ROOT, i, topic),
                    body = body,
                    timestamp = BASE_TIMESTAMP - i * HOUR_MILLIS,
                    labels = if (i % 7 == 0) emptyList() else listOf(LABELS[i % LABELS.size]),
                    color = COLORS[i % COLORS.size],
                    pinned = i % 25 == 0
                )
            )
        }
        return result
    }
}
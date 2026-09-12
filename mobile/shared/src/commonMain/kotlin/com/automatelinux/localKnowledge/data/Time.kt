package com.automatelinux.localKnowledge.data

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * The client owns the clock.
 *
 * MySQL writes column defaults in the server's zone, which read back as UTC put
 * every row three hours into the future — a bug tally paid for in real records.
 * So nothing here relies on the database's idea of "now": every timestamp is
 * written by whoever made the record, in UTC, in one format.
 */
private fun two(n: Int) = if (n < 10) "0$n" else "$n"

fun nowUtc(): String = utcString(Clock.System.now())

fun utcString(instant: Instant): String {
    val t = instant.toLocalDateTime(TimeZone.UTC)
    return "${t.year}-${two(t.monthNumber)}-${two(t.dayOfMonth)} " +
        "${two(t.hour)}:${two(t.minute)}:${two(t.second)}"
}

/** Parses what [utcString] wrote. Null for anything else — a stored value that is not
 *  a timestamp must not be silently read as the epoch, which would date it to 1970. */
fun parseUtc(value: String): Instant? {
    val s = value.trim().replace(' ', 'T')
    val withZone = if (s.endsWith("Z")) s else "${s}Z"
    return runCatching { Instant.parse(withZone) }.getOrNull()
}

/**
 * How old a finding is, in words.
 *
 * Age is the honest measure of how far to trust it: a tent spot confirmed last
 * month is a different claim from one written down four years ago and never
 * revisited. Showing the raw date would make the reader do that arithmetic; this
 * does it for them.
 */
fun ageLabel(confirmedAt: String, now: Instant = Clock.System.now()): String {
    val then = parseUtc(confirmedAt) ?: return "בזמן לא ידוע"
    val days = ((now - then).inWholeHours / 24).toInt()
    return when {
        days <= 0 -> "היום"
        days == 1 -> "אתמול"
        days < 7 -> "לפני $days ימים"
        days < 31 -> "לפני ${days / 7} שבועות"
        days < 365 -> "לפני ${days / 30} חודשים"
        days < 730 -> "לפני שנה"
        else -> "לפני ${days / 365} שנים"
    }
}

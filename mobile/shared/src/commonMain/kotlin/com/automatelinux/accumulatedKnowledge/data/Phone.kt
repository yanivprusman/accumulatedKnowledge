package com.automatelinux.accumulatedKnowledge.data

/**
 * The digits WhatsApp's `wa.me` link wants: country code, no plus, no dashes.
 *
 * Numbers get written the way they were read out — "050-2225880", "+972 50 222 5880" —
 * and a local Israeli number starts with the trunk 0 that `wa.me` does not understand.
 * Null when there are no digits at all, so no caller can build a link to nobody.
 */
fun internationalDigits(phone: String): String? {
    val digits = phone.filter { it.isDigit() }
    return when {
        digits.isEmpty() -> null
        digits.startsWith("00") -> digits.drop(2)
        digits.startsWith("0") -> "972" + digits.drop(1)
        else -> digits
    }
}

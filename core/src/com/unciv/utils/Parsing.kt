package com.unciv.utils

import yairm210.purity.annotations.Readonly

/**
 * Parse an Int but accept optional leading '+', and trim whitespace.
 * RoboVM's Integer.parseInt may reject leading '+', so we normalize.
 */
@Readonly
fun String.toIntLoose(): Int {
    val t = this.trim()
    val s = if (t.startsWith("+")) t.substring(1) else t
    return s.toInt()
}

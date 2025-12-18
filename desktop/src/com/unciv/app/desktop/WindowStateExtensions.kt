package com.unciv.app.desktop

import com.unciv.models.metadata.GameSettings.WindowState

/** Desktop-only helpers to interop with AWT types without leaking them into core. */
fun WindowState(bounds: java.awt.Rectangle): WindowState = WindowState(bounds.width, bounds.height)

fun WindowState.coerceIn(bounds: java.awt.Rectangle): WindowState = this.coerceIn(bounds.width, bounds.height)

package com.unciv.app.ios

import com.unciv.models.metadata.GameSettings
import com.unciv.utils.PlatformDisplay
import com.unciv.utils.ScreenMode
import com.unciv.utils.ScreenOrientation

class IOSDisplay : PlatformDisplay {
    private object DefaultMode : ScreenMode {
        override fun getId() = 0
        override fun hasUserSelectableSize() = false
        override fun toString() = "Default"
    }

    override fun getScreenModes(): Map<Int, ScreenMode> = mapOf(0 to DefaultMode)
    override fun setScreenMode(id: Int, settings: GameSettings) { /* noop */ }

    override fun hasOrientation() = true
    override fun setOrientation(orientation: ScreenOrientation) { /* iOS handled by Info.plist & Gdx */ }

    override fun hasUserSelectableSize(id: Int) = false
}

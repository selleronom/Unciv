package com.unciv.app.ios

import com.badlogic.gdx.backends.iosrobovm.IOSApplication
import com.badlogic.gdx.backends.iosrobovm.IOSApplicationConfiguration
import com.badlogic.gdx.graphics.glutils.HdpiMode
import com.unciv.UncivGame
import com.unciv.ui.components.fonts.Fonts
import com.unciv.utils.Display
import org.robovm.apple.foundation.NSAutoreleasePool
import org.robovm.apple.uikit.UIApplication

class IOSLauncher : IOSApplication.Delegate() {
    
    override fun createApplication(): IOSApplication {
        // Setup iOS platform components (must be done before creating UncivGame)
        Display.platform = IOSDisplay()
        
        // Setup iOS fonts
        Fonts.fontImplementation = IOSFont()
        
        val config = IOSApplicationConfiguration().apply {
            useAccelerometer = false
            useCompass = false
            hdpiMode = HdpiMode.Pixels
        }
        
        return IOSApplication(IOSGame(), config)
    }
    
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val pool = NSAutoreleasePool()
            UIApplication.main(args, UIApplication::class.java, IOSLauncher::class.java)
            pool.close()
        }
    }
}

class IOSGame : UncivGame() {
    override var customDataDirectory: String? = null
}

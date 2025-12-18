// iOS module uses plugins and repositories from root build

val gdxVersion: String by project
val coroutinesVersion: String by project

// RoboVM signing configuration for App Store/TestFlight
// Configure which certificate and provisioning profile to use
project.ext.set("robovm.iosSignIdentity", "iPhone Distribution")
project.ext.set("robovm.iosProvisioningProfile", "b736d7f5-670f-4105-876c-f2d786f5fd0f")

// Explicitly disable all test-related tasks
afterEvaluate {
    tasks.configureEach {
        if (name.contains("test", ignoreCase = true) || name.contains("Test")) {
            enabled = false
        }
    }
}

dependencies {
    implementation(project(":core"))
    implementation("com.badlogicgames.gdx:gdx-backend-robovm:$gdxVersion")
    implementation("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-ios")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
}

tasks.register("run") {
    dependsOn("launchIPhoneSimulator")
}

tasks.register("runDevice") {
    dependsOn("launchIOSDevice")
}

// Workaround task for Xcode 16/26 IPA creation bug
// This builds the app using the working path, then manually packages it
tasks.register("createIPAWorkaround") {
    group = "build"
    description = "Creates IPA using workaround for Xcode 16/26 (bypasses robovmArchive bug)"
    
    doLast {
        // Build the .app first using working build configuration
        println("Building iOS app for device (workaround for Xcode 16/26)...")
        
        // Execute the shell script that packages the IPA
        val scriptPath = projectDir.resolve("create-ipa-workaround.sh")
        if (!scriptPath.exists()) {
            throw GradleException("Workaround script not found: $scriptPath")
        }
        
        exec {
            workingDir = projectDir
            commandLine("bash", scriptPath.absolutePath)
        }
    }
}

// Eclipse project name is configured by root if needed

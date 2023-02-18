import jm.droid.compile.setupLibraryModule

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

setupLibraryModule(publish = true, document = false)

dependencies {
    implementation(libs.androidx.core)
    testImplementation(libs.junit)
}

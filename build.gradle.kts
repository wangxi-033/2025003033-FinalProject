plugins {
    id("com.android.application") version "8.7.3" apply false
    id("org.jetbrains.kotlin.android") version "2.1.0" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.0" apply false
    id("com.google.devtools.ksp") version "2.1.0-1.0.29" apply false
}

buildscript {
    extra.apply {
        set("lifecycle_version", "2.8.7")
        set("room_version", "2.6.1")
        set("navigation_version", "2.8.5")
        set("retrofit_version", "2.11.0")
        set("okhttp_version", "4.12.0")
        set("coil_version", "2.7.0")
        set("datastore_version", "1.1.1")
    }
}

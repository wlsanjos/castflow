// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // Provide Hilt Gradle plugin on the buildscript classpath so we can apply it
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.48")
        // Ensure a JavaPoet version that includes canonicalName() is available to the plugin
        classpath("com.squareup:javapoet:1.13.0")
    }
}

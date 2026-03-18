plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
}

// Apply the Hilt Gradle plugin from the buildscript classpath
apply(plugin = "dagger.hilt.android.plugin")

android {
    namespace = "com.wlsanjos.castflow"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.wlsanjos.castflow"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.5"
    }

    packagingOptions {
        resources {
            excludes += setOf(
                "META-INF/AL2.0", 
                "META-INF/LGPL2.1",
                "META-INF/INDEX.LIST",
                "META-INF/io.netty.versions.properties"
            )
        }
    }
}

dependencies {
    // Core
    implementation("androidx.core:core-ktx:1.10.1")

    // Compose & Material 3
    implementation("androidx.compose.ui:ui:1.5.0")
    implementation("androidx.compose.material3:material3:1.1.0")
    implementation("androidx.compose.material3:material3-window-size-class:1.1.0")
    implementation("androidx.activity:activity-compose:1.8.0")
    // Tooling & Icons
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.0")
    debugImplementation("androidx.compose.ui:ui-tooling:1.5.0")
    implementation("androidx.compose.material:material-icons-extended:1.4.1")

    // Android Material Components (styles used by themes XML)
    implementation("com.google.android.material:material:1.10.0")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.0")

    // Lifecycle / ViewModel
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.47")
    kapt("com.google.dagger:hilt-compiler:2.47")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    // Image loading
    implementation("io.coil-kt:coil-compose:2.4.0")

    // Networking
    implementation("com.squareup.okhttp3:okhttp:4.11.0")

    // Local Media Server (Ktor)
    implementation("io.ktor:ktor-server-core:2.3.3")
    implementation("io.ktor:ktor-server-netty:2.3.3")
    implementation("io.ktor:ktor-server-cio:2.3.3")
    implementation("io.ktor:ktor-io:2.3.3")
    implementation("io.ktor:ktor-http:2.3.3")
    implementation("io.ktor:ktor-utils:2.3.3")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}

kapt {
    correctErrorTypes = true
}

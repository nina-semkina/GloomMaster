plugins {
    alias(libs.plugins.chaosknight.android.application)
    alias(libs.plugins.chaosknight.android.application.compose)
    alias(libs.plugins.chaosknight.hilt)
    alias(libs.plugins.chaosknight.android.application.firebase)
    alias(libs.plugins.baselineprofile)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.room)
    alias(libs.plugins.kover)
}

android {
    enableKotlin = true
    namespace = "com.rumpilstilstkin.gloommaster"
    compileSdk = 37

    testBuildType = "benchmark"

    defaultConfig {
        applicationId = "com.rumpilstilstkin.gloommaster"
        minSdk = 29
        versionCode = 17
        versionName = "2.0.7"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        buildConfigField("boolean", "ONBOARDING_ENABLED", "true")
        manifestPlaceholders["firebase_crashlytics_enabled"] = "true"
        manifestPlaceholders["firebase_analytics_enabled"] = "true"
    }
    buildFeatures {
        buildConfig = true
        resValues = true
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("boolean", "ONBOARDING_ENABLED", "true")
            ndk.debugSymbolLevel = "FULL"
        }
        debug {
            manifestPlaceholders += mapOf()
            applicationIdSuffix = ".debug"
            manifestPlaceholders["appLabelSuffix"] = "(Debug)"
            buildConfigField("boolean", "ONBOARDING_ENABLED", "true")
            signingConfig = signingConfigs.getByName("debug")
        }
        create("benchmark") {
            initWith(buildTypes.getByName("release"))
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
            buildConfigField("boolean", "ONBOARDING_ENABLED", "false")
            configure<com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension> {
                mappingFileUploadEnabled = false
            }
            manifestPlaceholders["firebase_crashlytics_enabled"] = "false"
            manifestPlaceholders["firebase_analytics_enabled"] = "false"
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

baselineProfile {
    automaticGenerationDuringBuild = false
    dexLayoutOptimization = true
}

dependencyGuard {
    configuration("benchmarkReleaseCompileClasspath") {
        tree = true
    }
}

composeCompiler {
    reportsDestination = layout.buildDirectory.dir("compose_compiler")
    metricsDestination = layout.buildDirectory.dir("compose_compiler")
}

dependencies {
    implementation(project(":design-system"))
    implementation(project(":test-tags"))
    baselineProfile(project(":benchmark"))

    ksp(libs.room.compiler)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.splashscreen)
    implementation(libs.androidx.window.size)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.coil.compose)
    implementation(libs.androidx.profileinstaller)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.mockk)
    testImplementation(libs.strikt.core)
    testImplementation(libs.hilt.android.testing)

    androidTestImplementation(libs.room.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

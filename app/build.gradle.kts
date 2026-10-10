plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.harness"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.harness"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    flavorDimensions += "condition"
    productFlavors {
        create("conditionA") {
            dimension = "condition"
            applicationIdSuffix = ".a"
        }
        create("conditionB") {
            dimension = "condition"
            applicationIdSuffix = ".b"
        }
        create("conditionC") {
            dimension = "condition"
            applicationIdSuffix = ".c"
        }
    }

    buildTypes {
        getByName("debug") {
            isDebuggable = true
        }
        getByName("release") {
            isMinifyEnabled = false
        }
        create("measure") {
            initWith(getByName("release"))
            signingConfig = signingConfigs.getByName("debug")
            isMinifyEnabled = false
            isDebuggable = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.material)
    implementation(libs.kotlinx.coroutines.android)
}

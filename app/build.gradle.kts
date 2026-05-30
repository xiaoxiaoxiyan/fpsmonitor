apply(plugin = "com.android.application")
apply(plugin = "kotlin-android")

android {
    namespace = "com.xtoolbox"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.xtoolbox"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("androidx.navigation:navigation-compose:2.8.5")

    implementation("com.google.android.material:material:1.12.0")

    implementation("com.topjohnwu.libsu:core:5.2.2")
    implementation("com.topjohnwu.libsu:service:5.2.2")
    implementation("com.topjohnwu.libsu:nio:5.2.2")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    implementation("top.yukonga.miuix.kmp:miuix-ui-android:0.8.8")
    implementation("com.patrykandpatrick.vico:compose-m3:2.0.1")

    implementation("net.lingala.zip4j:zip4j:2.11.5")

    implementation("org.apache.commons:commons-compress:1.26.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}

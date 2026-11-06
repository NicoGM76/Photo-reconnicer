plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "com.nicogm.animalscan"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.nicogm.animalscan"
        minSdk = 26
        targetSdk = 28
        versionCode = 3
        versionName = "3.0"
    }
    flavorDimensions += "evidencia"
    productFlavors {
        create("v1") { dimension = "evidencia"; applicationIdSuffix = ".v1"; versionName = "1.0"; buildConfigField("int", "STAGE", "1") }
        create("v2") { dimension = "evidencia"; applicationIdSuffix = ".v2"; versionName = "2.0"; buildConfigField("int", "STAGE", "2") }
        create("v3") { dimension = "evidencia"; versionName = "3.0"; buildConfigField("int", "STAGE", "3") }
    }
    buildTypes {
        getByName("release") { isMinifyEnabled = false }
    }
    buildFeatures { buildConfig = true }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
    androidResources { noCompress += "tflite" }
}
dependencies {
    implementation("com.google.mediapipe:tasks-vision:0.10.14")
}
// El modelo oficial se descarga una sola vez, durante el primer build.
val modelPath = layout.projectDirectory.file("src/main/assets/efficientnet_lite0.tflite").asFile
val downloadAnimalModel = tasks.register("downloadAnimalModel") {
    outputs.file(modelPath)
    doLast {
        if (!modelPath.exists() || modelPath.length() < 1000000L) {
            modelPath.parentFile.mkdirs()
            val url = java.net.URI("https://storage.googleapis.com/mediapipe-models/image_classifier/efficientnet_lite0/int8/latest/efficientnet_lite0.tflite").toURL()
            val conn = url.openConnection()
            conn.connectTimeout = 30000
            conn.readTimeout = 120000
            conn.getInputStream().use { input -> modelPath.outputStream().use { output -> input.copyTo(output) } }
            check(modelPath.length() > 1000000L) { "Modelo incompleto" }
        }
    }
}
tasks.named("preBuild") { dependsOn(downloadAnimalModel) }

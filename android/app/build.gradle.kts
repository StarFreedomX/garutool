import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.OutputDirectory
import groovy.json.JsonSlurper

plugins {
    id("com.android.application")
}

abstract class GenerateToolResources : Exec() {
    @get:OutputDirectory abstract val resources: DirectoryProperty
    @get:OutputDirectory abstract val javaSources: DirectoryProperty
}

val toolkitOutput = layout.buildDirectory.dir("generated/toolkit")
val packageInfo = JsonSlurper().parseText(
    providers.fileContents(rootProject.layout.projectDirectory.file("../package.json")).asText.get()
) as Map<*, *>
val generateToolResources = tasks.register<GenerateToolResources>("generateToolResources") {
    val workspace = rootDir.parentFile
    inputs.files(fileTree(workspace.resolve("assets/icons")), workspace.resolve("src/config/tools.ts"))
    inputs.file(workspace.resolve("scripts/generate-native.mjs"))
    resources.set(toolkitOutput.map { it.dir("res") })
    javaSources.set(toolkitOutput.map { it.dir("java") })
    commandLine("node", workspace.resolve("scripts/generate-native.mjs"), toolkitOutput.get().asFile)
}

android {
    namespace = "io.github.garutool"
    compileSdk = 36

    defaultConfig {
        applicationId = "io.github.garutool"
        minSdk = 26
        targetSdk = 36
        versionCode = (packageInfo["androidVersionCode"] as Number).toInt()
        versionName = packageInfo["version"] as String
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
    }

}

androidComponents.onVariants { variant ->
    variant.sources.res?.addGeneratedSourceDirectory(generateToolResources, GenerateToolResources::resources)
    variant.sources.java?.addGeneratedSourceDirectory(generateToolResources, GenerateToolResources::javaSources)
}

dependencies {
    implementation(project(":capacitor-android"))
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.core:core:1.17.0")
}

apply(from = "capacitor.build.gradle")

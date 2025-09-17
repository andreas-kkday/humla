import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform

/*
 * Copyright (C) 2014 Andrew Comminos
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

plugins {
    id("com.android.library")
    alias(libs.plugins.kotlin.ksp)
    // Consider adding kotlin-android plugin if you use Kotlin in this module's source code
    // id("org.jetbrains.kotlin.android")
}

dependencies {
    api(libs.protobuf.java)
//    api("com.madgag.spongycastle:core:1.51.0.0")

    // Custom PKCS12 keybag parse modifications to support Mumble unencrypted certificates
    // Source: https://github.com/Morlunk/spongycastle/tree/pkcs12-keybag-fixes
//    api(files("libs/humla-spongycastle/prov/build/libs/prov-1.51.0.0.jar",
//              "libs/humla-spongycastle/pkix/build/libs/pkix-1.51.0.0.jar"))

    implementation(libs.spongycastle.core)
    implementation(libs.spongycastle.prov)
    implementation(libs.spongycastle.pkix)

    implementation(libs.javacpp)
    implementation(libs.jb.annotations)
    implementation(libs.minidns)
    implementation(libs.minidns.android21)
    implementation(libs.guava)
    testImplementation(libs.junit)

    //koin
    implementation(libs.koin.android)
    implementation(libs.koin.annotations)
    ksp(libs.koin.compiler)
}

// The allprojects block here will configure this project and its subprojects (if any).
// If this configuration is intended for all projects in the build,
// it should be moved to the root project's build.gradle.kts file.
//allprojects {
//    tasks.withType<JavaCompile> {
//        // TODO include deprecations at some point, but currently they are *many*
//        options.compilerArgs.addAll(listOf("-Xlint:all", "-Xlint:-deprecation", "-Xlint:-dep-ann"))
//    }
//}

android {
    namespace = "se.lublin.humla"

    compileSdk = 36
    ndkVersion = "28.1.13356709" // Make sure this NDK version is installed via SDK Manager
    // buildToolsVersion = "29.0.3" // buildToolsVersion is deprecated in AGP 7.0+

    sourceSets {
        getByName("main") {
            jniLibs.srcDir("src/main/libs/")
            jni.setSrcDirs(listOf<String>()) // Disable NDK build integration for this jni folder
        }
    }

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        testApplicationId = "se.lublin.humla.test"
        testInstrumentationRunner = "android.test.InstrumentationTestRunner"
        minSdk = 14
        targetSdk = 34

//        ndk {
//            abiFilters.addAll(listOf("armeabi", "armeabi-v7a", "x86"))
//            stl = "gnustl_static"
//            cFlags = "-I\$(LOCAL_PATH)/speex/include/ -I\$(LOCAL_PATH)/celt-0.11.0-src/include/ -I\$(LOCAL_PATH)/celt-0.7.0-src/include/ -I\$(LOCAL_PATH)/opus/include -D__EMX__ -DUSE_KISS_FFT -DFIXED_POINT -DEXPORT=\'\' -DHAVE_CONFIG_H -fvisibility=hidden -DOPUS_BUILD -DVAR_ARRAYS -Wno-traditional -DFIXED_POINT"
//        }
    }

    // Task for NDK build
    val ndkBuildTask = tasks.register<Exec>("ndkBuild") {
        val ndkDir =
            project.extensions.getByType(com.android.build.gradle.BaseExtension::class.java).ndkDirectory
        var ndkBuildCmd = "${ndkDir}/ndk-build"
        if (DefaultNativePlatform.getCurrentOperatingSystem().isWindows) {
            ndkBuildCmd += ".cmd"
        }
        commandLine(ndkBuildCmd, "-C", file("src/main/jni/").absolutePath)
    }

    tasks.withType<JavaCompile>().configureEach {
        dependsOn(ndkBuildTask)
    }
}
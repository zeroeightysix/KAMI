@file:Suppress("LocalVariableName")

import Build_gradle.DependencyMethod.IMPLEMENTATION
import Build_gradle.DependencyMethod.RUNTIME_ONLY
import Build_gradle.IncludeMethod.INCLUDE
import Build_gradle.IncludeMethod.NOT
import Build_gradle.IncludeMethod.SHADOW
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val minecraft_version: String by project
val mod_version: String by project
val maven_group: String by project

plugins {
    id("fabric-loom")
    id("maven-publish")
    id("com.github.johnrengelman.shadow") version "6.1.0"
    kotlin("jvm")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

version = "$minecraft_version-$mod_version"
group = maven_group

repositories {
    jcenter()
    mavenCentral()
    maven {
        name = "jitpack.io"
        url = uri("https://jitpack.io")
    }
    maven {
        name = "impactdevelopment-repo"
        url = uri("https://impactdevelopment.github.io/maven/")
    }
    maven {
        name = "Fabric"
        url = uri("http://maven.fabricmc.net/")
    }
}

enum class IncludeMethod {
    NOT, SHADOW, INCLUDE
}

enum class DependencyMethod {
    IMPLEMENTATION, RUNTIME_ONLY
}

dependencies {
    val kotlin_version: String by project
    val minecraft_version: String by project
    val yarn_mappings: String by project
    val loader_version: String by project
    val fiber_version: String by project
    val imgui_version: String by project

    fun depend(includeMethod: IncludeMethod = NOT, notation: String, dependencyMethod: DependencyMethod = IMPLEMENTATION, action: ExternalModuleDependency.() -> Unit = {}) {
        when (dependencyMethod) {
            IMPLEMENTATION -> implementation(dependencyNotation = notation, dependencyConfiguration = action)
            RUNTIME_ONLY -> runtimeOnly(dependencyNotation = notation, dependencyConfiguration = action)
        }

        when (includeMethod) {
            SHADOW -> shadow(dependencyNotation = notation, dependencyConfiguration = action)
            INCLUDE -> include(dependencyNotation = notation, dependencyConfiguration = action)
            else -> {
            }
        }
    }

    fun includedModImpl(dependencyNotation: String) {
        modImplementation(dependencyNotation)
        include(dependencyNotation)
    }

    // Fabric setup

    minecraft("com.mojang:minecraft:$minecraft_version")
    mappings("net.fabricmc:yarn:$yarn_mappings")
    modCompile("net.fabricmc:fabric-loader:$loader_version")
    includedModImpl("net.fabricmc.fabric-api:fabric-api-base:0.1.3+12a8474cfa")
    includedModImpl("net.fabricmc.fabric-api:fabric-resource-loader-v0:0.2.9+e5d3217f4e")
    includedModImpl("com.github.Ladysnake:Satin:1.5.0")

    // 1.16.4+ has not added any additional functionality and 1.16.3 Fabritone will work on newer versions.
    modImplementation("com.gitlab.CDAGaming:fabritone:fabric~1.16.3-SNAPSHOT") {
        exclude(group = "org.lwjgl")
        exclude(group = "org.lwjgl.lwjgl")
        exclude(group = "net.java.jinput")
        exclude(group = "net.sf.jopt-simple")
        exclude(group = "org.ow2.asm")
    }

    depend(INCLUDE, "com.github.fablabsmc:fiber:$fiber_version")
    depend(INCLUDE, "org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version")
    depend(INCLUDE, "org.jetbrains.kotlin:kotlin-reflect:$kotlin_version")

    depend(SHADOW, "org.reflections:reflections:0.9.11")
    depend(SHADOW, "com.github.ZeroMemes:Alpine:1.9")
    depend(SHADOW, "me.xdrop:fuzzywuzzy:1.3.1")

    // imgui
    depend(SHADOW, "io.imgui.java:imgui-java-binding:$imgui_version")
    depend(SHADOW, "io.imgui.java:imgui-java-lwjgl3:$imgui_version") {
        exclude(group = "org.lwjgl")
        exclude(group = "org.lwjgl.lwjgl")
    }
    arrayOf("linux-x86", "linux-x86", "macos", "windows", "windows-x86").forEach {
        depend(INCLUDE, "io.imgui.java:imgui-java-natives-$it:$imgui_version", RUNTIME_ONLY)
    }

    // Discord RPC
    depend(SHADOW, "com.github.Vatuu:discord-rpc:1.6.2")
}

tasks {
    java {
        withSourcesJar()
    }

    withType(JavaCompile::class) {
        options.encoding = "UTF-8"
    }

    withType(KotlinCompile::class) {
        kotlinOptions {
            jvmTarget = "11"
        }
    }

    processResources {
        inputs.property("version", version)

        from(project.the<SourceSetContainer>()["main"].resources.srcDirs) {
            include("fabric.mod.json")
            expand("version" to version)
        }
    }

    remapJar {
        dependsOn(":shadowJar")
        input.set(shadowJar.get().archiveFile)
    }

    shadowJar {
        configurations = listOf(project.configurations.shadow.get())

        exclude("/fonts/*")

        minimize()
    }

    build {
        doLast {
            shadowJar.get().archiveFile.get().asFile.delete()
        }
    }
}

configurations.shadow {
    isTransitive = false
}
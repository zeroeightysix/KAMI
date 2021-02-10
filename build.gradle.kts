@file:Suppress("LocalVariableName", "PropertyName")

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
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
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

dependencies {
    val minecraft_version: String by project
    val yarn_mappings: String by project
    val loader_version: String by project
    val api_version: String by project
    val resource_loader_version: String by project
    val kotlin_version: String by project
    val fiber_version: String by project
    val imgui_version: String by project
    val satin_version: String by project
    val reflections_version: String by project
    val alpine_version: String by project
    val fuzzywuzzy_version: String by project

    fun depend(includeMethod: IncludeMethod = NOT, notation: String, action: ExternalModuleDependency.() -> Unit = {}) {
        implementation(dependencyNotation = notation, dependencyConfiguration = action)
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
    includedModImpl("net.fabricmc.fabric-api:fabric-api-base:$api_version")
    includedModImpl("net.fabricmc.fabric-api:fabric-resource-loader-v0:$resource_loader_version")
    includedModImpl("com.github.Ladysnake:Satin:$satin_version")

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
    depend(INCLUDE, "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version")
    depend(INCLUDE, "org.jetbrains.kotlin:kotlin-reflect:$kotlin_version")

    depend(SHADOW, "org.reflections:reflections:$reflections_version")
    depend(SHADOW, "com.github.ZeroMemes:Alpine:$alpine_version")
    depend(SHADOW, "me.xdrop:fuzzywuzzy:$fuzzywuzzy_version")

    // imgui
    depend(SHADOW, "io.imgui.java:imgui-java-binding:$imgui_version")
    depend(SHADOW, "io.imgui.java:imgui-java-lwjgl3:$imgui_version") {
        exclude(group = "org.lwjgl")
        exclude(group = "org.lwjgl.lwjgl")
    }
    arrayOf("linux", "linux-x86", "macos", "windows", "windows-x86").forEach {
        depend(SHADOW, "io.imgui.java:imgui-java-natives-$it:$imgui_version")
    }

    // Discord RPC
    depend(SHADOW, "com.github.Vatuu:discord-rpc:1.6.2")

    // TODO: javassist is broken with minimize
    listOf(
        "org.javassist:javassist:3.21.0-GA",
        "net.jodah:typetools:0.5.0",
        "org.jetbrains:annotations:13.0"
    ).forEach {
        shadow(it)
    }
}

tasks {
    java {
        withSourcesJar()
    }

    withType(JavaCompile::class) {
        options.encoding = "UTF-8"
    }

    withType<KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = "1.8"
            freeCompilerArgs += "-Xopt-in=kotlin.io.path.ExperimentalPathApi"
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
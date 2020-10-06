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
    id("com.github.johnrengelman.shadow") version "5.2.0"
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
    val kotlin_version: String by project
    val minecraft_version: String by project
    val yarn_mappings: String by project
    val loader_version: String by project
    val fiber_version: String by project
    val kg_version: String by project
    val glm_version: String by project
    val uno_version: String by project
    val kool_version: String by project
    val unsigned_version: String by project
    val gli_version: String by project
    val gln_version: String by project

    fun depend(method: IncludeMethod = NOT, notation: String, action: ExternalModuleDependency.() -> Unit = {}) {
        implementation(dependencyNotation = notation, dependencyConfiguration = action)
        when (method) {
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
    modImplementation("com.gitlab.CDAGaming:fabritone:fabritone~1.16.x-Fabric-SNAPSHOT")

    depend(INCLUDE, "com.github.fablabsmc:fiber:$fiber_version")
    depend(INCLUDE, "org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version")
    depend(INCLUDE, "org.jetbrains.kotlin:kotlin-reflect:$kotlin_version")

    depend(SHADOW, "com.github.kotlin-graphics:kotlin-unsigned:$unsigned_version")
    depend(SHADOW, "com.github.kotlin-graphics:kool:$kool_version")
    depend(SHADOW, "org.reflections:reflections:0.9.11")
    depend(SHADOW, "com.github.ZeroMemes:Alpine:1.9")
    depend(SHADOW, "com.github.kotlin-graphics:imgui:$kg_version") {
        exclude(group = "org.lwjgl")
    }
    depend(SHADOW, "com.github.kotlin-graphics:glm:$glm_version")
    depend(SHADOW, "com.github.kotlin-graphics:uno-sdk:$uno_version") {
        exclude(group = "org.lwjgl")
    }
    depend(SHADOW, "me.xdrop:fuzzywuzzy:1.3.1")

    // Discord RPC
    depend(SHADOW, "com.github.Vatuu:discord-rpc:1.6.2")

    // We disable shadowing transitive dependencies because imgui pulls in over a hundred of them, many of which we never need.
    // Unfortunately shadow's `minimize` does not remove these classes, so we manually add the ones we do use.
    listOf(
        "org.javassist:javassist:3.21.0-GA",
        "net.jodah:typetools:0.5.0",
        "org.jetbrains:annotations:13.0",
        "com.github.kotlin-graphics:gln:$gln_version",
        "com.github.kotlin-graphics:gli:$gli_version",
        "com.github.kotlin-graphics.imgui:core:$kg_version",
        "com.github.kotlin-graphics.imgui:glfw:$kg_version",
        "com.github.kotlin-graphics.imgui:gl:$kg_version",
        "com.github.kotlin-graphics.uno-sdk:core:$uno_version"
    ).forEach {
        shadow(it)
    }
}

tasks {
    withType(JavaCompile::class) {
        options.encoding = "UTF-8"
    }

    withType(KotlinCompile::class) {
        kotlinOptions {
            jvmTarget = "1.8"
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
        archiveClassifier.set("shadow")

        exclude("/fonts/*")

        minimize()
    }
}

configurations.shadow {
    isTransitive = false
}

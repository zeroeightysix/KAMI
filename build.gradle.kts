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
        name = "modmuss50's repo"
        url = uri("https://maven.modmuss50.me/")
    }
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

    fun implAndShadow(notation: String) {
        implementation(notation)
        shadow(notation)
    }

    // Fabric setup

    minecraft("com.mojang:minecraft:$minecraft_version")
    mappings("net.fabricmc:yarn:$yarn_mappings")
    modCompile("net.fabricmc:fabric-loader:$loader_version")
    modImplementation("net.fabricmc.fabric-api:fabric-resource-loader-v0:0.2.9+e5d3217f4e")

    implementation("com.github.fablabsmc:fiber:$fiber_version")
    implementation("com.github.ZeroMemes:Alpine:1.9")
    shadow("com.github.ZeroMemes:Alpine:1.9")

    implementation("org.reflections:reflections:0.9.11") {
        exclude("com.google.guava:guava")
    }
    shadow("org.reflections:reflections:0.9.11")

    implementation("com.github.kotlin-graphics:imgui:$kg_version")
    implementation("com.github.kotlin-graphics:glm:$glm_version")
    implementation("com.github.kotlin-graphics:uno-sdk:$uno_version")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlin_version")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version")

    shadow("net.jodah:typetools:0.5.0")

    shadow("com.github.zeroeightysix.imgui:imgui-core:$kg_version")
    shadow("com.github.zeroeightysix.imgui:imgui-gl:$kg_version")
    shadow("com.github.zeroeightysix.imgui:imgui-glfw:$kg_version")
    shadow("com.github.kotlin-graphics:glm:$glm_version")
    shadow("com.github.kotlin-graphics.uno-sdk:uno:$uno_version")
    shadow("com.github.kotlin-graphics.uno-sdk:uno-core:$uno_version")
    shadow("com.github.kotlin-graphics:kool:$kool_version")
    shadow("com.github.kotlin-graphics:kotlin-unsigned:$unsigned_version")
    shadow("com.github.kotlin-graphics:gln:$kg_version")
    shadow("com.github.kotlin-graphics:gli:$gli_version")

    include("com.github.fablabsmc:fiber:$fiber_version")
    include("org.jetbrains.kotlin:kotlin-stdlib:")
    include("org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version")
    include("org.jetbrains.kotlin:kotlin-stdlib-common:$kotlin_version")
    include("org.jetbrains.kotlin:kotlin-reflect:$kotlin_version")
    include("org.javassist:javassist:3.21.0-GA")
    include("org.jetbrains:annotations:13.0")
    include("net.fabricmc.fabric-api:fabric-resource-loader-v0:0.2.9+e5d3217f4e")
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
    }
}

configurations {
    "shadow" {
        isTransitive = false
    }
}

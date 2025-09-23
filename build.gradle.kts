import org.gradle.kotlin.dsl.get
import org.gradle.jvm.toolchain.JavaLanguageVersion

plugins {
    id("java-library")
    id("fabric-loom") version "1.11-SNAPSHOT"
    id("com.modrinth.minotaur") version "2.+"
}

class ModData {
    val id = property("mod.id").toString()
    val name = property("mod.name").toString()
    val version = property("mod.version").toString()
    val group = property("mod.group").toString()
    val mc_dep = property("mod.mc_dep").toString()
}

class ModDependencies {
    operator fun get(name: String) = property("deps.$name").toString()
}

group = project.extra["maven_group"] as String

base.archivesName.set("${project.extra["archives_base_name"]}-${stonecutter.current.version}")

val mod = ModData()
val deps = ModDependencies()
val mcVersion = stonecutter.current.version

java.sourceCompatibility = JavaVersion.VERSION_21
java.targetCompatibility = JavaVersion.VERSION_21

modrinth {
    token.set(System.getenv("MODRINTH_TOKEN") ?: project.findProperty("modrinth_token")?.toString())
    projectId.set(project.findProperty("modrinth_project_id")?.toString() ?: "your-project-id")
    versionNumber.set(mod.version)
    versionName.set("${mod.name} ${mod.version}")
    versionType.set("release") // or "beta", "alpha"
    uploadFile.set(tasks.remapJar)

    gameVersions.set(listOf(stonecutter.current.version))
    loaders.set(listOf("fabric"))

    dependencies {
        required.project("fabric-api")
        optional.project("modmenu")
        optional.project("cloth-config")
    }

    // Read changelog from file
    changelog.set(provider {
        val changelogFile = file("../../changelogs/${mod.version}.md")
        if (changelogFile.exists()) {
            changelogFile.readText()
        } else {
            throw GradleException("changelog file not found: ${changelogFile.path}")
        }
    })
}


repositories {

    maven { url = uri("https://maven.terraformersmc.com/releases/") }
    maven { url = uri("https://maven.shedaniel.me/") }
    maven { url = uri("https://maven.wispforest.io") }
    maven {
        name = "CottonMC"
        url = uri("https://server.bbkr.space/artifactory/libs-release")
    }
    maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/") {
        name = "sonatype-oss-snapshots1"
        mavenContent { snapshotsOnly() }
    }
    mavenCentral()
    maven {
        name = "ParchmentMC"
        url = uri("https://maven.parchmentmc.org")
    }

    // Dev only
    maven { url = uri("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1") }
}

loom {
    splitEnvironmentSourceSets()
    mods {
        register("highlighter") {
            sourceSet("main")
            sourceSet("client")
        }
    }
    runConfigs {
        named("client") {
            ideConfigGenerated(true)
            runDir = "../../runs/${stonecutter.current.version}"
            vmArgs("-Dminecraft.suppressGLErrors=true")
            vmArgs("-Dfabric.development=true")
            vmArgs("-Ddevauth.enabled=true")
        }

        all {
            if (name != "client") {
                ideConfigGenerated(false)
            }
        }
    }
}

dependencies {
    implementation("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")

    // Minecraft dependencies
    minecraft("com.mojang:minecraft:${stonecutter.current.project}")
    mappings("net.fabricmc:yarn:${property("deps.yarn_mappings")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")
    val fapi = deps["fabric_api"]
    val miniMessage = deps["minimessage_version"]
    val clothConfig = deps["cloth_config_version"]
    val modMenu = deps["mod_menu_version"]
    val owo = deps["owo_version"]


    modImplementation("net.fabricmc.fabric-api:fabric-api:$fapi")


    modImplementation(include("net.kyori:adventure-platform-fabric:$miniMessage")!!)
    modApi("me.shedaniel.cloth:cloth-config-fabric:$clothConfig") {
        exclude(group = "net.fabricmc.fabric-api")
    }
    modApi("com.terraformersmc:modmenu:$modMenu")

    modImplementation("io.wispforest:owo-lib:$owo") {
        exclude(group = "net.fabricmc.fabric-api", module = "fabric-api")
    }
    annotationProcessor("io.wispforest:owo-lib:$owo")
    include("io.wispforest:owo-lib:$owo")



    implementation("com.moulberry:mixinconstraints:1.0.8")?.let { include(it) }

    // Dev only
    runtimeOnly("me.djtheredstoner:DevAuth-fabric:${project.findProperty("devauth_version")}")
}


tasks {
    processResources {
        inputs.property("version", mod.version)
        inputs.property("minecraft_version", stonecutter.current.version)
        inputs.property("mc",mod.mc_dep)
        inputs.property("loader_version", project.extra["deps.fabric_loader"])
        inputs.property("cloth_config_version", project.extra["deps.cloth_config_version"])
        inputs.property("mod_menu_version", project.extra["deps.mod_menu_version"])
        inputs.property("owo_version", project.extra["deps.owo_version"])
        filteringCharset = "UTF-8"

        filesMatching("fabric.mod.json") {
            expand(
                mapOf(
                    "version" to mod.version,
                    "minecraft_version" to stonecutter.current.version,
                    "mc" to mod.mc_dep,
                    "fabric_version" to project.extra["deps.fabric_api"],
                    "loader_version" to project.extra["deps.fabric_loader"],
                    "cloth_config_version" to project.extra["deps.cloth_config_version"],
                    "mod_menu_version" to project.extra["deps.mod_menu_version"],
                    "owo_version" to project.extra["deps.owo_version"]
                )
            )
        }
    }

    withType<JavaCompile> {
        options.release.set(21)
    }
}

tasks.register<Copy>("buildAndCollect") {
    group = "versioned"
    description = "Must run through 'chiseledBuild'"
    from(tasks.remapJar.get().archiveFile) // Only main JAR, no sources
    into(rootProject.layout.buildDirectory.file("libs/"))
    dependsOn("build")
}
tasks.register("publishToModrinth") {
    group = "publishing"
    description = "Publish current version to Modrinth"
    dependsOn("build")
    finalizedBy("modrinth")
}

// Task that can be used with chiseled
tasks.register("chiseledPublish") {
    group = "versioned"
    description = "Publish all versions to Modrinth (use with stonecutter chiseled)"
    dependsOn("publishToModrinth")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${project.the<BasePluginExtension>().archivesName.get()}" }
    }
}

//publishing {
//	publications {
//		create<MavenPublication>("mavenJava") {
//			artifactId = project.the<BasePluginExtension>().archivesName.get()
//			from(components["java"])
//		}
//	}
//
//	repositories {
//		// Add repositories to publish to here.
//	}
//}

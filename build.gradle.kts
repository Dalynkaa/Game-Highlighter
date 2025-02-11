plugins {
    id("java-library")
    id("fabric-loom") version "1.9-SNAPSHOT"
}

version = project.extra["mod_version"] as String
group = project.extra["maven_group"] as String

base.archivesName.set("${project.extra["archives_base_name"]}-${project.extra["minecraft_version"]}")




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

    // Dev only
    maven { url = uri("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1") }
}

loom {
    splitEnvironmentSourceSets()
    mods {
        register("gamehighlighter") {
            sourceSet("main")
            sourceSet("client")
        }
    }
}

dependencies {
    implementation("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")

    // Minecraft dependencies
    minecraft("com.mojang:minecraft:${project.findProperty("minecraft_version")}")
    mappings("net.fabricmc:yarn:${project.findProperty("yarn_mappings")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${project.findProperty("loader_version")}")

    // Fabric API
    modImplementation("net.fabricmc.fabric-api:fabric-api:${project.findProperty("fabric_version")}")

    // Mod dependencies
    modApi("com.terraformersmc:modmenu:${project.findProperty("mod_menu_version")}")
    modApi("me.shedaniel.cloth:cloth-config-fabric:${project.findProperty("cloth_config_version")}") {
        exclude(group = "net.fabricmc.fabric-api")
    }

    // owo lib
    modImplementation("io.wispforest:owo-lib:${project.findProperty("owo_version")}")
    annotationProcessor("io.wispforest:owo-lib:${project.findProperty("owo_version")}")
    include("io.wispforest:owo-lib:${project.findProperty("owo_version")}")

    implementation("com.moulberry:mixinconstraints:1.0.1")
    include("com.moulberry:mixinconstraints:1.0.1")

    // MiniMessage
    modImplementation("net.kyori:adventure-platform-fabric:${project.findProperty("minimessage_version")}")
    include("net.kyori:adventure-platform-fabric:${project.findProperty("minimessage_version")}")


    // Dev only
    modRuntimeOnly("me.djtheredstoner:DevAuth-fabric:${project.findProperty("devauth_version")}")
}


tasks {
    processResources {
        inputs.property("version", project.version)
        inputs.property("minecraft_version", project.extra["minecraft_version"])
        inputs.property("loader_version", project.extra["loader_version"])
        inputs.property("cloth_config_version", project.extra["cloth_config_version"])
        inputs.property("mod_menu_version", project.extra["mod_menu_version"])
        inputs.property("owo_version", project.extra["owo_version"])
        filteringCharset = "UTF-8"

        filesMatching("fabric.mod.json") {
            expand(
                mapOf(
                    "version" to project.version,
                    "minecraft_version" to project.extra["minecraft_version"],
                    "fabric_version" to project.extra["fabric_version"],
                    "loader_version" to project.extra["loader_version"],
                    "cloth_config_version" to project.extra["cloth_config_version"],
                    "mod_menu_version" to project.extra["mod_menu_version"],
                    "owo_version" to project.extra["owo_version"]
                )
            )
        }
    }

    withType<JavaCompile> {
        options.release.set(21)
    }
}

java {
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

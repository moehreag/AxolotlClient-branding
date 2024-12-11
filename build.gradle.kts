import org.gradle.jvm.tasks.Jar
import java.nio.file.Files
import kotlin.io.path.deleteExisting
import kotlin.io.path.listDirectoryEntries

plugins {
	id("io.freefair.lombok") version "8.10" apply false
	id("com.modrinth.minotaur") version "2.+" apply false
	id("com.gradleup.shadow") version "8.+" apply false
	id("dev.yumi.gradle.licenser") version "2.0.+"
	id("io.github.p03w.machete") version "2.+" apply false
	id("fabric-loom") version "1.8.+" apply false
	id("ploceus") version "1.8.+" apply false
}

version = "${project.version}"
group = "io.github.axolotlclient"

repositories {
	mavenLocal()
	maven {
		url = uri("https://moehreag.duckdns.org/maven/releases")
	}
	mavenCentral()
}

dependencies {
}

allprojects {

	repositories {
		maven {
			name = "TerraformersMC Maven"
			url = uri("https://maven.terraformersmc.com/releases/")
		}
		maven {
			name = "Quilt"
			url = uri("https://maven.quiltmc.org/repository/release")
		}
		maven {
			url = uri("https://moehreag.duckdns.org/maven/releases")
		}
		maven {
			url = uri("https://moehreag.duckdns.org/maven/snapshots")
		}
		mavenLocal()
		maven { url = uri("https://jitpack.io") }
		mavenCentral()
	}
}

subprojects {
	apply(plugin = "java")
	apply(plugin = "maven-publish")
	apply(plugin = "io.freefair.lombok")
	apply(plugin = "com.modrinth.minotaur")
	apply(plugin = "dev.yumi.gradle.licenser")

	extensions.getByType(JavaPluginExtension::class).withSourcesJar()

	tasks.getByName("jar", Jar::class) {
		filesMatching("LICENSE") {
			rename("^(LICENSE.*?)(\\..*)?$", "\$1_${archiveBaseName}\$2")
		}
	}

	license {
		rule(file("../HEADER"))
		include("**/*.java")
	}

	tasks.register("collectBuilds") {
		if (project.name == "common") {
			enabled = false
		}
		val outDir = rootProject.layout.buildDirectory.dir("collected").get().asFile.toPath()
		outDir.listDirectoryEntries().forEach { it.deleteExisting() }
		Files.createDirectories(outDir)
		actions.addLast {
			project.layout.buildDirectory.dir("libs").get().asFileTree.files.forEach {file ->
				if (file.name.endsWith(project.version.toString()+".jar")) {
					Files.copy(file.toPath(), outDir.resolve(file.name.toString()))
				}
			}
		}
	}
}




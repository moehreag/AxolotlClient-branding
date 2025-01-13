import org.gradle.jvm.tasks.Jar
import java.nio.file.FileSystems
import kotlin.io.path.*

plugins {
	id("io.freefair.lombok") version "8.11" apply false
	id("com.modrinth.minotaur") version "2.+" apply false
	id("com.gradleup.shadow") version "8.+" apply false
	id("dev.yumi.gradle.licenser") version "2.0.+"
	id("io.github.p03w.machete") version "2.+" apply false
	id("fabric-loom") version "1.9.+" apply false
	id("ploceus") version "1.9.+" apply false
}

version = "${project.version}"
group = "io.github.axolotlclient"

repositories {
	maven {
		url = uri("https://moehreag.duckdns.org/maven/releases")
	}
	mavenCentral()
}

allprojects {
	repositories {
		maven("https://maven.terraformersmc.com/releases")
		maven("https://maven.fabricmc.net")
		maven("https://maven.quiltmc.org/repository/release")
		maven("https://moehreag.duckdns.org/maven/releases")
		maven("https://moehreag.duckdns.org/maven/snapshots")
		maven("https://maven.parchmentmc.org")
		maven("https://repo.hypixel.net/repository/Hypixel/") {
			content {
				includeGroup("net.hypixel")
			}
		}
		maven("https://api.modrinth.com/maven") {
			content {
				includeGroup("maven.modrinth")
			}
		}
		mavenLocal()
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
		dependsOn(tasks.getByName("build"))
		if (project.name == "common") {
			enabled = false
		}
		actions.addLast {
			val outDir = rootProject.projectDir.resolve("builds").toPath()
			outDir.createDirectories()
			val archiveDir = outDir.resolve("archive")
			outDir.listDirectoryEntries().forEach { old ->
				if (!old.isRegularFile()) {
					return@forEach
				}
				val oldName = old.fileName.toString()
				val oldVer = oldName.substring(0, oldName.indexOf("+"))
				val mcVer = oldName.substring(oldName.indexOf("+")+1, oldName.length-4).removeSuffix("-sources")
				if (!project.version.toString().contains(mcVer)) {
					return@forEach
				}
				// check if it's the current version, if it is we don't archive it
				if (project.version.toString().contains(oldVer.substring(oldVer.indexOf("-")+1))) {
					return@forEach
				}
				archiveDir.createDirectories()
				val versionArchive = archiveDir.resolve("$oldVer.zip")
				synchronized(rootProject) {
					(if (versionArchive.notExists()) {
						FileSystems.newFileSystem(versionArchive, mapOf("create" to "true"))
					} else {
						FileSystems.newFileSystem(versionArchive)
					}).use {
						old.moveTo(it.getPath(oldName))
					}
				}
			}
			project.layout.buildDirectory.dir("libs").get().asFileTree.files.forEach { file ->
				if (file.name.contains(project.version.toString())) {
					file.toPath().copyTo(outDir.resolve(file.name.toString()), overwrite = true)
				}
			}
		}
	}
}




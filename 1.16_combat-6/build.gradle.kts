plugins {
	id("fabric-loom")
	id("io.github.p03w.machete")
}

group = project.property("maven_group")!!
version = "${project.property("version")}+${project.property("minecraft_cts8")}"
base.archivesName = "AxolotlClient"

repositories {
	maven("https://maven.fabric.rizecookey.net/")
}

loom {
	intermediaryUrl.set("https://maven.fabric.rizecookey.net/net/fabricmc/intermediary/1.16_combat-6/intermediary-1.16_combat-6-v2.jar")

	mods {
		create("axolotlclient") {
			sourceSet("main")
		}
		create("axolotlclient-test") {
			sourceSet("test")
		}
	}
}

dependencies {
	minecraft("com.mojang:minecraft:${project.property("minecraft_cts8")}")
	mappings("net.fabricmc:yarn:${project.property("mappings_cts8")}:v2")

	modImplementation("net.fabricmc:fabric-loader:${project.property("fabric_loader")}")

	modImplementation("net.fabricmc.fabric-api:fabric-networking-api-v1:1.0.5+3cc0f0907d")
	modImplementation("net.fabricmc.fabric-api:fabric-command-api-v1:1.1.3+3cc0f0907d")
	modImplementation("net.fabricmc.fabric-api:fabric-lifecycle-events-v1:1.2.2+3cc0f0907d")
	modImplementation("net.fabricmc.fabric-api:fabric-resource-loader-v0:0.4.8+3cc0f0907d")
	modImplementation("net.fabricmc.fabric-api:fabric-api-base:0.4.0+3cc0f0907d")

	modImplementation("io.github.axolotlclient:AxolotlClient-config:${project.property("config")}+1.16") {
		exclude(group = "com.terraformersmc")
		exclude(group = "org.lwjgl")
	}
	include("io.github.axolotlclient:AxolotlClient-config:${project.property("config")}+1.16")
	modImplementation("io.github.axolotlclient.AxolotlClient-config:AxolotlClientConfig-common:${project.property("config")}")

	modCompileOnlyApi("io.github.prospector:modmenu:1.14.9+build.14")

	implementation(include(project(path = ":common", configuration = "shadow"))!!)

	include("org.apache.logging.log4j:log4j-slf4j-impl:2.0-beta9") {
		exclude(group = "org.apache.logging.log4j", module = "log4j-api")
		exclude(group = "org.apache.logging.log4j", module = "log4j-core")
	}
	implementation(include("org.slf4j:slf4j-api:1.7.36")!!)
	localRuntime("org.slf4j:slf4j-jdk14:1.7.36")

	api("org.lwjgl:lwjgl-nanovg:3.2.2")
	runtimeOnly("org.lwjgl:lwjgl-nanovg:3.2.2:natives-linux")
	runtimeOnly("org.lwjgl:lwjgl-nanovg:3.2.2:natives-windows")
	runtimeOnly("org.lwjgl:lwjgl-nanovg:3.2.2:natives-macos")
}

tasks.processResources {
	inputs.property("version", version)

	filesMatching("fabric.mod.json") {
		expand("version" to version)
	}
}

tasks.runClient {
	classpath(sourceSets.getByName("test").runtimeClasspath)
}

tasks.withType(JavaCompile::class).configureEach {
	options.encoding = "UTF-8"

	if (JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_18)) {
		options.release = 17
	}
}

java {
	sourceCompatibility = JavaVersion.VERSION_17
	targetCompatibility = JavaVersion.VERSION_17
}

// Configure the maven publication
publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			artifactId = base.archivesName.get()
			from(components["java"])
		}
	}

	repositories {
		maven {
			name = "owlMaven"
			val repository = if (project.version.toString().contains("beta") || project.version.toString()
					.contains("alpha")
			) "snapshots" else "releases"
			url = uri("https://moehreag.duckdns.org/maven/$repository")
			credentials(PasswordCredentials::class)
			authentication {
				create<BasicAuthentication>("basic")
			}
		}
	}
}

tasks.modrinth {
	dependsOn(tasks.getByName("optimizeOutputsOfRemapJar"))
}

modrinth {
	token = System.getenv("MODRINTH_TOKEN")
	projectId = "p2rxzX0q"
	versionNumber = "${project.version}"
	versionType = "release"
	uploadFile = tasks.remapJar.get()
	gameVersions.set(listOf("1.16.3"))
	loaders.set(listOf("fabric", "quilt"))
	additionalFiles.set(listOf(tasks.remapSourcesJar))
	dependencies {
		required.project("fabric-api")
	}

	// Changelog fetching: Credit LambdAurora.
	// https://github.com/LambdAurora/LambDynamicLights/blob/1ef85f486084873b5d97b8a08df72f57859a3295/build.gradle#L145
	// License: MIT
	val changelogText = file("../CHANGELOG.md").readText()
	val regexVersion =
		((project.version) as String).split("+")[0].replace("\\.".toRegex(), "\\.").replace("\\+".toRegex(), "+")
	val changelogRegex = "###? ${regexVersion}\\n\\n(( *- .+\\n)+)".toRegex()
	val matcher = changelogRegex.find(changelogText)

	if (matcher != null) {
		var changelogContent = matcher.groups[1]?.value

		val changelogLines = changelogText.split("\n")
		val linkRefRegex = "^\\[([A-z0-9 _\\-/+.]+)]: ".toRegex()
		for (line in changelogLines.reversed()) {
			if ((linkRefRegex.matches(line)))
				changelogContent += "\n" + line
			else break
		}
		changelog = changelogContent
	} else {
		afterEvaluate {
			tasks.modrinth.configure {isEnabled = false}
		}
	}
}

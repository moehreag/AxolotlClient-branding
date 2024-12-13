plugins {
	id("fabric-loom")
	id("io.github.p03w.machete")
}

group = project.property("maven_group") as String
version = "${project.property("version")}+${project.property("minecraft_121")}"
base.archivesName = "AxolotlClient"

loom {
	accessWidenerPath.set(file("src/main/resources/axolotlclient.accesswidener"))
	mods {
		create("axolotlclient") {
			sourceSet("main")
		}
		create("axolotlclient-test") {
			sourceSet("test")
		}
	}
}

repositories {
	exclusiveContent {
		forRepository {
			maven {
				name = "Modrinth"
				url = uri("https://api.modrinth.com/maven")
			}
		}
		maven {
			name = "Noxcrew/Public"
			url = uri("https://maven.noxcrew.com/public")
		}
		maven("https://maven.enginehub.org/repo/")
		filter {
			includeGroup("maven.modrinth")
		}
	}
}

dependencies {
	minecraft("com.mojang:minecraft:${project.property("minecraft_121")}")
	mappings("org.quiltmc:quilt-mappings:${project.property("mappings_121")}:intermediary-v2")

	modImplementation("net.fabricmc:fabric-loader:${project.property("fabric_loader")}")

	modImplementation("net.fabricmc.fabric-api:fabric-api:${project.property("fapi_121")}+${project.property("minecraft_121")}")

	modImplementation("io.github.axolotlclient:AxolotlClient-config:${project.property("config")}+${project.property("minecraft_121")}") {
		exclude(group = "com.terraformersmc")
		exclude(group = "org.lwjgl")
	}
	include("io.github.axolotlclient:AxolotlClient-config:${project.property("config")}+${project.property("minecraft_121")}")
	modImplementation("io.github.axolotlclient.AxolotlClient-config:AxolotlClientConfig-common:${project.property("config")}")

	modCompileOnlyApi("com.terraformersmc:modmenu:8.0.0") {
		exclude(group = "net.fabricmc")
	}

	implementation(include(project(path = ":common", configuration = "shadow"))!!)

	api("org.lwjgl:lwjgl-nanovg:3.3.3")
	runtimeOnly("org.lwjgl:lwjgl-nanovg:3.3.3:natives-linux")
	runtimeOnly("org.lwjgl:lwjgl-nanovg:3.3.3:natives-linux-arm64")
	runtimeOnly("org.lwjgl:lwjgl-nanovg:3.3.3:natives-windows")
	runtimeOnly("org.lwjgl:lwjgl-nanovg:3.3.3:natives-windows-arm64")
	runtimeOnly("org.lwjgl:lwjgl-nanovg:3.3.3:natives-macos")
	runtimeOnly("org.lwjgl:lwjgl-nanovg:3.3.3:natives-macos-arm64")

	modCompileOnly("maven.modrinth:world-host:0.5.0+1.21.1-fabric")
	//implementation("org.quiltmc.parsers:json:0.3.0")
	//implementation("org.semver4j:semver4j:5.3.0")

	val noxesiumVersion = "2.3.3"
	modCompileOnly("maven.modrinth:noxesium:$noxesiumVersion")
	//modImplementation("com.noxcrew.noxesium:api:$noxesiumVersion")
	//localRuntime("org.khelekore:prtree:1.5")
}

tasks.processResources {
	inputs.property("version", version)

	filesMatching("fabric.mod.json") {
		expand("version" to version)
	}
}

java {
	sourceCompatibility = JavaVersion.VERSION_21
	targetCompatibility = JavaVersion.VERSION_21
}

tasks.runClient {
	classpath(sourceSets.getByName("test").runtimeClasspath)
}

// Configure the maven publication
publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			artifactId = project.name
			from(components["java"])
		}
	}

	repositories {
		maven {
			name = "owlMaven"
			val repository = if (project.version.toString().contains("beta") || project.version.toString().contains("alpha")) "snapshots" else "releases"
			url = uri("https://moehreag.duckdns.org/maven/$repository")
			credentials(PasswordCredentials::class)
			authentication {
				create<BasicAuthentication>("basic")
			}
		}
	}
}

modrinth {
	token = System.getenv("MODRINTH_TOKEN")
	projectId = "p2rxzX0q"
	versionNumber = "${project.version}"
	versionType = "release"
	uploadFile = tasks.remapJar.get()
	gameVersions.set(listOf("${project.property("minecraft_121")}"))
	loaders.set(listOf("quilt", "fabric"))
	additionalFiles.set(listOf(tasks.remapSourcesJar))

	// Changelog fetching: Credit LambdAurora.
	// https://github.com/LambdAurora/LambDynamicLights/blob/1ef85f486084873b5d97b8a08df72f57859a3295/build.gradle#L145
	// License: MIT
	val changelogText = file("../CHANGELOG.md").readText()
	val regexVersion = ((project.version) as String).split("\\+")[0].replace("\\.".toRegex(), "/\\./").replace("\\+".toRegex(), "\\+")
	val changelogRegex = "###? ${regexVersion}\\n\\n(( *- .+\\n)+)".toRegex()
	val matcher = changelogRegex.find(changelogText)

	if (matcher != null) {
		var changelogContent = matcher.groups[1]?.value

		val changelogLines = changelogText.split("\n")
		val linkRefRegex = "^\\[([A-z0-9 _\\-/+.]+)]: ".toRegex()
		for (i in (changelogLines.size -1)..0 step -1) {
			val line = changelogLines[i]
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

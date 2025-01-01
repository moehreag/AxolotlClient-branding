plugins {
	id("fabric-loom")
	id("io.github.p03w.machete")
}

group = project.property("maven_group")!!
version = "${project.property("version")}+${project.property("minecraft_120")}"
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
	maven("https://maven.noxcrew.com/public")
	maven("https://maven.enginehub.org/repo/")
}

dependencies {
	minecraft("com.mojang:minecraft:${project.property("minecraft_120")}")
	mappings("org.quiltmc:quilt-mappings:${project.property("mappings_120")}:intermediary-v2")

	modImplementation("net.fabricmc:fabric-loader:${project.property("fabric_loader")}")

	modImplementation("net.fabricmc.fabric-api:fabric-api:${project.property("fapi_120")}+${project.property("minecraft_120")}")

	modImplementation("io.github.axolotlclient:AxolotlClient-config:${project.property("config")}+${project.property("minecraft_120")}") {
		exclude(group = "com.terraformersmc")
		exclude(group = "org.lwjgl")
	}
	include("io.github.axolotlclient:AxolotlClient-config:${project.property("config")}+${project.property("minecraft_120")}")
	modImplementation("io.github.axolotlclient.AxolotlClient-config:AxolotlClientConfig-common:${project.property("config")}")

	modCompileOnlyApi("com.terraformersmc:modmenu:8.0.0") {
		exclude(group = "net.fabricmc")
	}

	implementation(include(project(path = ":common", configuration = "shadow"))!!)

	api("org.lwjgl:lwjgl-nanovg:3.3.2")
	runtimeOnly("org.lwjgl:lwjgl-nanovg:3.3.2:natives-linux")
	runtimeOnly("org.lwjgl:lwjgl-nanovg:3.3.2:natives-linux-arm64")
	runtimeOnly("org.lwjgl:lwjgl-nanovg:3.3.2:natives-windows")
	runtimeOnly("org.lwjgl:lwjgl-nanovg:3.3.2:natives-windows-arm64")
	runtimeOnly("org.lwjgl:lwjgl-nanovg:3.3.2:natives-macos")
	runtimeOnly("org.lwjgl:lwjgl-nanovg:3.3.2:natives-macos-arm64")

	val noxesiumVersion = "1.0.3"
	modCompileOnly("maven.modrinth:noxesium:$noxesiumVersion")
	//modImplementation("com.noxcrew.noxesium:api:$noxesiumVersion")
	//localRuntime("org.khelekore:prtree:1.5")

	implementation("net.hypixel:mod-api:1.0.1")
	include(modImplementation("io.github.moehreag.hypixel:mod-api-fabric:1.0.1+build.2+mc1.20.1")!!)
}

tasks.processResources {
	inputs.property("version", version)

	filesMatching("fabric.mod.json") {
		expand("version" to version)
	}
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

tasks.runClient {
	classpath(sourceSets.getByName("test").runtimeClasspath)
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
			val repository = if (project.version.toString().contains("beta") || project.version.toString().contains("alpha")) "snapshots" else "releases"
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
	gameVersions.set(listOf("${project.property("minecraft_120")}"))
	loaders.set(listOf("quilt", "fabric"))
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

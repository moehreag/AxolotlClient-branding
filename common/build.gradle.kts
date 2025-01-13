plugins {
	id("java")
	id("com.gradleup.shadow")
}

group = project.property("maven_group").toString()+"."+project.property("archives_base_name").toString()
base.archivesName.set(project.property("archives_base_name").toString()+"-common")

dependencies {
	compileOnly("net.fabricmc:fabric-loader:${project.property("fabric_loader")}")
	compileOnly("org.jetbrains:annotations:24.0.0")

	// take the oldest version just to build against
	testRuntimeOnly(compileOnly("io.github.axolotlclient:AxolotlClient-config:${project.property("config")}+${project.property("minecraft_18")}") {
		isTransitive = false
	})
	testRuntimeOnly(testCompileOnly(compileOnly("io.github.axolotlclient.AxolotlClient-config:AxolotlClientConfig-common:${project.property("config")}")!!)!!)

	testRuntimeOnly(compileOnly("com.google.guava:guava:17.0")!!)
	testImplementation(compileOnly("org.apache.httpcomponents:httpclient:4.3.3")!!)
	testImplementation(compileOnly("com.google.code.gson:gson:2.2.4")!!)
	testRuntimeOnly(compileOnly("commons-io:commons-io:2.4")!!)
	testRuntimeOnly(compileOnly("org.apache.commons:commons-lang3:3.3.2")!!)
	testRuntimeOnly(compileOnly("org.lwjgl:lwjgl-glfw:3.3.2")!!)

	shadow(implementation("io.github.CDAGaming:DiscordIPC:0.10.2"){
		isTransitive = false
	})
	shadow(implementation("com.kohlschutter.junixsocket:junixsocket-common:2.10.1")!!)
	shadow(implementation("com.kohlschutter.junixsocket:junixsocket-native-common:2.10.1")!!)

	shadow(implementation("com.github.mizosoft.methanol:methanol:1.8.0")!!)
	shadow(implementation("io.nayuki:qrcodegen:1.8.0")!!)
}

tasks.jar {
	enabled = false
}

tasks.build {
	dependsOn(tasks.shadowJar)
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

tasks.withType(AbstractArchiveTask::class).configureEach {
	isPreserveFileTimestamps = false
	isReproducibleFileOrder = true
}

tasks.shadowJar {
	archiveClassifier.set("")
	mergeServiceFiles()
	minimize {
		exclude(dependency("com.github.mizosoft.methanol:.*:.*"))
		exclude(dependency("io.github.CDAGaming:DiscordIPC:.*"))
		exclude(dependency("com.kohlschutter.junixsocket:junixsocket-common:.*"))
		exclude(dependency("com.kohlschutter.junixsocket:junixsocket-native-common:.*"))
	}

	relocate("com.jagrosh", "io.github.axolotlclient.shadow.jagrosh")
	relocate("com.github.mizosoft", "io.github.axolotlclient.shadow.mizosoft")
	relocate("io.nayuki", "io.github.axolotlclient.shadow.nayuki")

	append("../LICENSE")
}

java {
	sourceCompatibility = JavaVersion.VERSION_17
	targetCompatibility = JavaVersion.VERSION_17
}

publishing {
	publications {
		create("shadow", MavenPublication::class) {
			artifactId = base.archivesName.get()
			from(components["shadow"])
		}
	}
	repositories {
		maven {
			name = "owlMaven"
			val repository = if(project.version.toString().contains("beta") || project.version.toString().contains("alpha")) "snapshots" else "releases"
			url = uri("https://moehreag.duckdns.org/maven/$repository")
			credentials(PasswordCredentials::class)
			authentication {
				create<BasicAuthentication>("basic")
			}
		}
	}
}

tasks.modrinth {
	enabled = false
}

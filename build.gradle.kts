plugins {
	id("net.fabricmc.fabric-loom")
	`maven-publish`
}

version = providers.gradleProperty("mod_version").get()
group = providers.gradleProperty("maven_group").get()

repositories {
	maven {
		url = uri("https://maven.minecraftforge.net/")
	}
	maven {
		url = uri("https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/")
	}
	maven {
		url = uri("https://cursemaven.com")
		content {
			includeGroup("curse.maven")
		}
	}
	maven {
		name = "Modrinth"
		url = uri("https://api.modrinth.com/maven")
		content {
			includeGroup("maven.modrinth")
		}
	}
}

fabricApi {
	configureDataGeneration {
		client = true
	}
}

dependencies {
	// To change the versions see the gradle.properties file
	minecraft("com.mojang:minecraft:${providers.gradleProperty("minecraft_version").get()}")

	implementation("net.fabricmc:fabric-loader:${providers.gradleProperty("loader_version").get()}")

	// Fabric API. This is technically optional, but you probably want it anyway.
	implementation("net.fabricmc.fabric-api:fabric-api:${providers.gradleProperty("fabric_api_version").get()}")

	implementation("com.github.glitchfiend:TerraBlender-fabric:26.1.2-26.1.2.0.1")
	implementation("com.geckolib:geckolib-fabric-${providers.gradleProperty("minecraft_version").get()}:${providers.gradleProperty("geckolib_version").get()}")

	runtimeOnly("maven.modrinth:sodium:${providers.gradleProperty("sodium_version").get()}")
	runtimeOnly("maven.modrinth:iris:${providers.gradleProperty("iris_version").get()}")
}

tasks.processResources {
	val version = version
	inputs.property("version", version)

	filesMatching("fabric.mod.json") {
		expand("version" to version)
	}
}

tasks.withType<JavaCompile>().configureEach {
	options.release = 25
}

java {
	withSourcesJar()
	sourceCompatibility = JavaVersion.VERSION_25
	targetCompatibility = JavaVersion.VERSION_25
}

tasks.jar {
	val projectName = project.name
	inputs.property("projectName", projectName)

	from("LICENSE") {
		rename { "${it}_$projectName" }
	}
}

publishing {
	publications {
		register<MavenPublication>("mavenJava") {
			from(components["java"])
		}
	}
	repositories {
		// Add repositories to publish to here.
	}
}
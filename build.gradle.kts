plugins {
    `java-gradle-plugin`
    `maven-publish`
    id("com.gradle.plugin-publish") version "0.20.0"
    id("org.jetbrains.kotlin.jvm") version "1.6.10"
}

version = "0.0.2"
group = "dev.nies.gradle.template"

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")

    implementation("org.freemarker:freemarker:2.3.31")
    implementation("org.apache.velocity:velocity:1.7")
    implementation("org.apache.velocity:velocity-tools:2.0")
}

gradlePlugin {
    val template by plugins.creating {
        id = "dev.nies.gradle.template"
        implementationClass = "dev.nies.gradle.template.TemplatePlugin"
    }
}

// Add a source set, task, and dependency for 'check' on the functional test suite
val functionalTestSourceSet = sourceSets.create("functionalTest") {
}
configurations["functionalTestImplementation"].extendsFrom(configurations["testImplementation"])
val functionalTest by tasks.registering(Test::class) {
    testClassesDirs = functionalTestSourceSet.output.classesDirs
    classpath = functionalTestSourceSet.runtimeClasspath
}
gradlePlugin.testSourceSets(functionalTestSourceSet)
tasks.named<Task>("check") {
    dependsOn(functionalTest)
}

pluginBundle {
    website = "https://github.com/arocnies/gradle-template"
    vcsUrl = "https://github.com/arocnies/gradle-template"
    description = "Adds templating tasks for use in Gradle builds. " +
            "Templating engines include: Freemarker. WIP: Velocity, Thymeleaf, Pebble, KorTE"
    (plugins) {
        "template" {
            displayName = "Template Plugin"
            tags = listOf("template", "properties", "Freemarker", "Velocity", "Thymeleaf")
        }
    }
}

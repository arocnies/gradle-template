plugins {
    `java-gradle-plugin`
    id("org.jetbrains.kotlin.jvm") version "1.6.10"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")

    implementation("org.freemarker:freemarker:2.3.31")
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

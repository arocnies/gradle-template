package dev.anies.gradle.template

import org.gradle.api.file.FileTree
import org.gradle.api.tasks.Input
import java.io.Serializable
import java.nio.file.Path

abstract class TemplateEngine<out T> : Serializable {
    abstract fun load(fileTree: FileTree)
    abstract fun processTemplate(source: Path, destination: Path, data: Map<String, Any?>): Boolean
    abstract fun configure(block: T.() -> Unit = {})

    /**
     * Protected to avoid exposing settings out into the task definition.
     * This exists to allow Gradle to cache a serializable view of an engine.
     */
    @get:Input
    protected abstract val settings: Map<String, Serializable?>
}
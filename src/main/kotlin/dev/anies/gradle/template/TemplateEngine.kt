package dev.anies.gradle.template

import org.gradle.api.file.FileTree
import java.io.Serializable
import java.nio.file.Path

interface TemplateEngine<out T> : Serializable {
    fun load(fileTree: FileTree)
    fun processTemplate(source: Path, destination: Path, data: Map<String, Any?>): Boolean
    fun configure(block: T.() -> Unit = {})
}
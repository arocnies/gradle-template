package dev.anies.gradle.template

import org.gradle.api.file.FileTree
import java.nio.file.Path

interface TemplateEngine {
    fun load(fileTree: FileTree)
    fun processTemplate(source: Path, destination: Path, data: Map<String, Any?>): Boolean
}
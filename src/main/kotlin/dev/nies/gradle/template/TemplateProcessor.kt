package dev.nies.gradle.template

import org.gradle.api.file.FileTree
import java.nio.file.Path

interface TemplateProcessor {
    fun load(fileTree: FileTree)
    fun processTemplate(source: Path, destination: Path, data: Map<String, Any?>): Boolean
}
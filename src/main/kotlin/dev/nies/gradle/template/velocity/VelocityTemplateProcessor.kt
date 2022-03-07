package dev.nies.gradle.template.velocity

import dev.nies.gradle.template.TemplateProcessor
import org.apache.velocity.Template
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import org.gradle.api.file.FileTree
import java.nio.file.Path
import kotlin.io.path.writer

class VelocityTemplateProcessor : TemplateProcessor {
    private val engine = VelocityEngine()
    private val templates = mutableMapOf<String, Template>()

    init {
        engine.addProperty("resource.loader", "file")
        engine.addProperty(
            "file.resource.loader.class",
            "org.apache.velocity.runtime.resource.loader.FileResourceLoader"
        )
        engine.addProperty("file.resource.loader.path", "")
        engine.init()
    }

    override fun load(fileTree: FileTree) {
        templates.clear()

        fileTree.associateTo(templates) {
            it.name to engine.getTemplate(it.absolutePath)
        }
    }

    override fun processTemplate(source: Path, destination: Path, data: Map<String, Any?>): Boolean {
        val template = templates[source.fileName.toString()] ?: return false
        val context = VelocityContext(data)
        destination.writer().use {
            template.merge(context, it)
        }
        return true
    }
}
package dev.anies.gradle.template.velocity

import dev.anies.gradle.template.TemplateEngine
import org.apache.velocity.Template
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import org.gradle.api.file.FileTree
import java.nio.file.Path
import kotlin.io.path.writer

class VelocityTemplateEngine : TemplateEngine<VelocityEngine> {
    private lateinit var engine: VelocityEngine
    private val templates = mutableMapOf<String, Template>()

    init {
        configure()
    }

    override fun configure(block: VelocityEngine.() -> Unit) {
        val engineBuilder = VelocityEngine()
        engineBuilder.addProperty("resource.loader", "file")
        engineBuilder.addProperty(
            "file.resource.loader.class",
            "org.apache.velocity.runtime.resource.loader.FileResourceLoader"
        )
        engineBuilder.addProperty("file.resource.loader.path", "")
        engineBuilder.block()
        engineBuilder.init()
        this.engine = engineBuilder
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
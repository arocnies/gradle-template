package dev.anies.gradle.template.velocity

import dev.anies.gradle.template.TemplateEngine
import org.apache.velocity.Template
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import org.gradle.api.file.FileTree
import java.io.Serializable
import java.nio.file.Path
import kotlin.io.path.writer

class VelocityTemplateEngine : TemplateEngine<VelocityEngine>() {
    private lateinit var engine: VelocityEngine
    private val templates = mutableMapOf<String, Template>()

    override val settings: Map<String, Serializable?>
        get() = emptyMap() // TODO: Switch the properties to be saved instead of added one-by-one to the engineBuilder.

    init {
        configure()
    }

    override fun configure(block: VelocityEngine.() -> Unit) {
        val engineBuilder = VelocityEngine()
        engineBuilder.addProperty("resource.loader", "file")
        engineBuilder.addProperty(
            "file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.FileResourceLoader"
        )
        engineBuilder.addProperty("file.resource.loader.path", "")
        engineBuilder.block()

        // We must use another class loader to avoid Velocity bug that causes a failure when
        // Velocity loads its logger from multiple modules.
        // See (https://stackoverflow.com/questions/45006042/velocity-initialization-failing)
        usingClassLoader(javaClass.classLoader) {
            engineBuilder.init()
        }
        this.engine = engineBuilder
    }

    /**
     * Switches the current thread's class loader in the context of the block.
     */
    private fun usingClassLoader(classLoader: ClassLoader, block: () -> Unit) {
        val currentThread = Thread.currentThread()
        val savedClassLoader = currentThread.contextClassLoader
        try {
            currentThread.contextClassLoader = classLoader
            block()
        } finally {
            currentThread.contextClassLoader = savedClassLoader
        }
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

@Suppress("unused")
@JvmOverloads
fun velocity(config: VelocityEngine.() -> Unit = {}) = VelocityTemplateEngine().also { it.configure(config) }
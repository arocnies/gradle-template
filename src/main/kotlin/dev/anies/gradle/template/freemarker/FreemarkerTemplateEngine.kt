package dev.anies.gradle.template.freemarker

import dev.anies.gradle.template.FileTreeTemplateLoader
import dev.anies.gradle.template.TemplateEngine
import freemarker.template.Configuration
import freemarker.template.Version
import org.gradle.api.file.FileTree
import java.io.Serializable
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.notExists
import kotlin.io.path.writer

class FreemarkerTemplateEngine : TemplateEngine<Configuration>() {
    @Transient
    private lateinit var configuration: Configuration

    /*
    We accept exposing this deprecated method to the parent protected field since it's the only reasonable way to
    capture Freemarker Configuration for serializable comparison and it will not be used outside this class.
    */
    @Suppress("DEPRECATION", "UNCHECKED_CAST")
    override val settings: Map<String, Serializable?>
        get() = configuration.settings as Map<String, Serializable?>

    init {
        configure()
    }

    override fun configure(block: Configuration.() -> Unit) {
        val config = Configuration(Version("2.3.31")).apply {
            defaultEncoding = "UTF-8"
            templateExceptionHandler = freemarker.template.TemplateExceptionHandler.RETHROW_HANDLER
            logTemplateExceptions = false
            fallbackOnNullLoopVariable = false
            block()
        }
        configuration = config
    }

    override fun load(fileTree: FileTree) {
        configuration.templateLoader = FileTreeTemplateLoader(fileTree)
    }

    override fun processTemplate(source: Path, destination: Path, data: Map<String, Any?>): Boolean {
        destination.parent.createDirectories()
        if (destination.notExists()) destination.createFile()
        destination.writer().use {
            configuration.getTemplate(getTemplateKey(source)).process(data, it)
        }
        return true
    }

    private fun getTemplateKey(path: Path): String {
        return path.fileName.toString()
    }
}

@Suppress("unused")
@JvmOverloads
fun freemarker(config: Configuration.() -> Unit = {}) = FreemarkerTemplateEngine().also { it.configure(config) }
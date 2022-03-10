package dev.anies.gradle.template.freemarker

import dev.anies.gradle.template.FileTreeTemplateLoader
import dev.anies.gradle.template.TemplateEngine
import freemarker.template.Configuration
import freemarker.template.Version
import org.gradle.api.file.FileTree
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.notExists
import kotlin.io.path.writer

class FreemarkerTemplateEngine : TemplateEngine<Configuration> {
    private var configuration: Configuration? = null

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
        val config = configuration
            ?: throw IllegalStateException("Not configured. Cannot load templates before being configured.")

        config.templateLoader = FileTreeTemplateLoader(fileTree)
    }

    override fun processTemplate(source: Path, destination: Path, data: Map<String, Any?>): Boolean {
        val config = configuration
            ?: throw IllegalStateException("Not configured. Cannot process templates before being configured.")

        destination.parent.createDirectories()
        if (destination.notExists()) destination.createFile()
        destination.writer().use {
            config.getTemplate(getTemplateKey(source)).process(data, it)
        }
        return true
    }
}

private fun getTemplateKey(path: Path): String {
    return path.fileName.toString()
}
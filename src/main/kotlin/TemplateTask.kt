import freemarker.cache.TemplateLoader
import freemarker.template.Configuration
import freemarker.template.Version
import org.gradle.api.file.FileTree
import org.gradle.api.internal.file.copy.CopyAction
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.WorkResults
import java.io.File
import java.io.IOException
import java.io.Reader
import java.nio.charset.Charset
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.notExists
import kotlin.io.path.writeText

abstract class TemplateTask : Copy() {
    override fun createCopyAction(): CopyAction {
        val sources = mainSpec.buildRootResolver().allSource
        val loader = FileTreeTemplateLoader(sources)
//        val multiTemplateLoader = MultiTemplateLoader(
//            sources.filter { it.isFile }.map { FileTemplateLoader(it) }.toTypedArray()
//        )
        val templateConfig = Configuration(Version("2.3.31")).apply {
            templateLoader = loader
            defaultEncoding = "UTF-8"
            templateExceptionHandler = freemarker.template.TemplateExceptionHandler.RETHROW_HANDLER
            logTemplateExceptions = false
            fallbackOnNullLoopVariable = false
        }

        return CopyAction { stream ->
            stream.process { details ->
                if (!details.isDirectory) {
                    println("DEBUG: " + details.file)
                    println("DEBUG: " + details.file.readText())
                    val templateKey = details.file.name
                    val destRelPath = destinationDir.toPath().resolve(details.path)
                    println("DEBUG: DestRelPath " + destRelPath)


                    destRelPath.apply {
                        parent.createDirectories()
                        if (notExists()) createFile()
                        writeText(templateConfig.getTemplate(templateKey).toString())
                    }
                }
            }
            WorkResults.didWork(false)
        }
    }
}

private class FileTreeTemplateLoader(private val fileTree: FileTree) : TemplateLoader {
    private val readers = mutableMapOf<String, Reader>()

    override fun findTemplateSource(name: String?): Any? {
        if (name.isNullOrBlank()) return null
        return fileTree.associateBy { getTemplateKey(it) }[name]
    }

    override fun getLastModified(templateSource: Any?): Long {
        if (templateSource !is File) return -1
        return templateSource.lastModified()
    }

    override fun getReader(templateSource: Any, encoding: String?): Reader {
        if (templateSource !is File) throw IOException("Unable to get reader for $templateSource file")
        return getReader(templateSource, encoding)
    }

    override fun closeTemplateSource(templateSource: Any?) {
        if (templateSource !is File) throw IllegalArgumentException("Bad templateSource type ${templateSource?.javaClass} when expecting File")
        readers[getTemplateKey(templateSource)]?.close()
    }

    private fun getTemplateKey(file: File): String {
//        return file.toPath().absolute().relativize(Path.of(fileTree.asPath)).toString()
        return file.name
    }

    private fun getReader(file: File, encoding: String?): Reader {
        if (readers.containsKey(getTemplateKey(file))) readers[getTemplateKey(file)]?.close()
        val charset = if (encoding != null) charset(encoding) else Charset.defaultCharset()
        return file.reader(charset)
    }
}

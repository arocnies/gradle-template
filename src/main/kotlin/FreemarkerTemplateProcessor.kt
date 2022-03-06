import freemarker.template.Configuration
import freemarker.template.Version
import org.gradle.api.file.FileTree
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.notExists

class FreemarkerTemplateProcessor : TemplateProcessor {
    private val configuration = Configuration(Version("2.3.31")).apply {
        defaultEncoding = "UTF-8"
        templateExceptionHandler = freemarker.template.TemplateExceptionHandler.RETHROW_HANDLER
        logTemplateExceptions = false
        fallbackOnNullLoopVariable = false
    }

    override fun load(fileTree: FileTree) {
        configuration.templateLoader = FileTreeTemplateLoader(fileTree)
    }

    override fun processTemplate(source: Path, destination: Path, data: Map<String, Any?>): Boolean {
        destination.parent.createDirectories()
        if (destination.notExists()) destination.createFile()
        val writer = destination.toFile().writer()
        configuration.getTemplate(getTemplateKey(source)).process(data, writer)
        return true
    }
}

private fun getTemplateKey(path: Path): String {
    return path.fileName.toString()
}
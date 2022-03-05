import freemarker.template.Configuration
import freemarker.template.Version
import org.gradle.api.internal.file.copy.CopyAction
import org.gradle.api.tasks.*
import java.io.File
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.notExists
import kotlin.io.path.relativeTo

abstract class TemplateTask : Copy() {
    @Internal
    val configuration = Configuration(Version("2.3.31")).apply {
        defaultEncoding = "UTF-8"
        templateExceptionHandler = freemarker.template.TemplateExceptionHandler.RETHROW_HANDLER
        logTemplateExceptions = false
        fallbackOnNullLoopVariable = false
    }

    private val _templatedFiles = mutableListOf<File>()
    val templatedFiles: List<File>
        @OutputFiles get() = _templatedFiles

    @Input
    var data = mutableMapOf<String, Any?>()

    override fun createCopyAction(): CopyAction {
        configuration.templateLoader = FileTreeTemplateLoader(mainSpec.buildRootResolver().allSource)

        return CopyAction { stream ->
            stream.process { details ->
                if (!details.isDirectory) {
                    val sourcePath = details.file.toPath()
                    val destPath = destinationDir.toPath().resolve(details.path)
                    processTemplate(sourcePath, destPath)
                }
            }
            WorkResults.didWork(true)
        }
    }

    private fun processTemplate(source: Path, destination: Path) = destination.apply {
        println(
            "Processing template " +
                    "\"${source.relativeTo(project.projectDir.toPath())}\" " +
                    "into \"${destination.relativeTo(project.projectDir.toPath())}\""
        )

        parent.createDirectories()
        if (notExists()) createFile()
        val writer = this.toFile().writer()
        configuration.getTemplate(getTemplateKey(source)).process(getTemplateProperties(), writer)
        _templatedFiles += source.toFile()
    }

    private fun getTemplateKey(path: Path): String {
        return path.fileName.toString()
    }

    private fun getTemplateProperties(): Map<String, *> {
        return data.toMutableMap() + ("properties" to project.properties)
    }
}
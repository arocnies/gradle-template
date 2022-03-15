package dev.anies.gradle.template

import org.gradle.api.internal.file.copy.CopyAction
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.WorkResults
import java.io.File
import java.nio.file.Path
import kotlin.io.path.relativeTo

@Suppress("unused")
abstract class TemplateTask : Copy() {
    @Suppress("unused")
    val templatedFiles: List<File>
        @OutputFiles get() = _templatedFiles

    @Input
    var data = mutableMapOf<String, Any?>()

    @Input
    lateinit var engine: TemplateEngine<*>
    private val _templatedFiles = mutableListOf<File>()

    override fun createCopyAction(): CopyAction {
        val templateEngine: TemplateEngine<*> = engine
        templateEngine.configure()
        templateEngine.load(mainSpec.buildRootResolver().allSource)
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

    private fun processTemplate(source: Path, destination: Path) {
        val templateEngine: TemplateEngine<*> = engine
        println(
            "Processing template \"${source.relativeTo(project.projectDir.toPath())}\" into \"${
                destination.relativeTo(project.projectDir.toPath())
            }\""
        )
        val didTemplate = templateEngine.processTemplate(source, destination, getTemplateData())
        if (didTemplate) _templatedFiles += destination.toFile()
    }

    private fun getTemplateData(): Map<String, *> {
        return data.toMutableMap() + ("properties" to project.properties)
    }
}
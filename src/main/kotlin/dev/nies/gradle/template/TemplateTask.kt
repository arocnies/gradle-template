package dev.nies.gradle.template

import dev.nies.gradle.template.freemarker.FreemarkerTemplateProcessor
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

    private val _templatedFiles = mutableListOf<File>()
    private val templateProcessor: TemplateProcessor = FreemarkerTemplateProcessor()

    override fun createCopyAction(): CopyAction {
        templateProcessor.load(mainSpec.buildRootResolver().allSource)
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
        println(
            "Processing template \"${source.relativeTo(project.projectDir.toPath())}\" into \"${
                destination.relativeTo(project.projectDir.toPath())
            }\""
        )
        val didTemplate = templateProcessor.processTemplate(source, destination, getTemplateData())
        if (didTemplate) _templatedFiles += destination.toFile()
    }

    private fun getTemplateData(): Map<String, *> {
        return data.toMutableMap() + ("properties" to project.properties)
    }
}
import org.gradle.api.Project
import org.gradle.api.Plugin
import java.io.File
import java.io.StringWriter
import java.nio.file.Paths
import freemarker.template.Configuration
import freemarker.template.Version

class BuildTemplatesPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // Register a task
        project.tasks.register("template") { task ->
            val inFiles = project.fileTree("${project.projectDir}/src/templates")
            val cfg = Configuration(Version("2.3.31")).apply {
                setDirectoryForTemplateLoading(File("${project.projectDir}/src/templates"))
                defaultEncoding = "UTF-8"
                templateExceptionHandler = freemarker.template.TemplateExceptionHandler.RETHROW_HANDLER
                logTemplateExceptions = false
                fallbackOnNullLoopVariable = false
            }

            fun fetchTemplateProperties() = project.properties
            var lastUsedTemplateProperties: Map<String, *>? = null
            task.doLast { lastUsedTemplateProperties = fetchTemplateProperties() }
            task.outputs.upToDateWhen { lastUsedTemplateProperties == fetchTemplateProperties() }
            task.outputs.dir("${project.buildDir}/templates")
            task.doLast {
                // Two sync/copies are used because: Writing content within an 'eachFile' modifies the original file.
                //  therefore, we first sync the files into the correct dir, then run the templating in another sync task.
                //  Gradle actions of sync/copy are used to more easy hook into the Gradle Task changes and CopySpec standard.
                project.sync {
                    it.from(inFiles)
                    it.into("${project.buildDir}/templates")
                }
                project.sync { copySpec ->
                    copySpec.from("${project.buildDir}/templates")
                    copySpec.into("${project.buildDir}/templates")
                    copySpec.rename("(.+).ftl", "$1")
                    copySpec.eachFile {
                        val templatesBasePath = Paths.get("${project.buildDir}/templates")
                        val relativeFilePath = templatesBasePath.relativize(it.file.toPath())
                        val template = cfg.getTemplate(relativeFilePath.toString())
                        StringWriter().use { out ->
                            template.process(project.properties, out)
                            println(out)
                            it.file.writeText(out.toString())
                        }
                    }
                }
            }
        }
    }
}

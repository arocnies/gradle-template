import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.readText
import kotlin.io.path.writer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TemplateTaskFunctionalTest {
    @get:Rule
    val tempFolder = TemporaryFolder()

    private fun getProjectDir() = tempFolder.root
    private fun getBuildFile() = getProjectDir().resolve("build.gradle")
    private fun getSettingsFile() = getProjectDir().resolve("settings.gradle")
    private fun getPropertiesFile() = getProjectDir().resolve("gradle.properties")
    private fun getTemplateTestOutput() = getProjectDir().resolve("build/templates/example.txt")

    data class ExpectedFile(
        val source: String,
        val dest: String,
        val initialContent: String,
        val expectedContent: String
    )

    @Test
    fun `Templates file with properties value`() {
        val expectedFiles = setupTestProject()
        val result = runBuild()
        verifyResult(result, expectedFiles)
    }

    private fun setupTestProject(): List<ExpectedFile> {
        writeGradleProjectFiles()

        val sources = listOf(
            ExpectedFile(
                source = "src/templates/example.txt.ftl",
                dest = "build/templates/example.txt",
                initialContent = "Hello ${'$'}{properties.example}\nThis is a ${'$'}{test}",
                expectedContent = "Hello World!\nThis is a template"
            ),
            ExpectedFile(
                source = "src/copy/taskFile.txt",
                dest = "build/templates/taskFile.txt",
                initialContent = "TaskFile ${'$'}{properties.example}\nThis is a ${'$'}{test}",
                expectedContent = "TaskFile World!\nThis is a template"
            )
        )
        writeSourceFiles(sources)
        return sources
    }

    private fun writeSourceFiles(sources: List<ExpectedFile>) {
        for (source in sources) {
            val path = getProjectDir().toPath().resolve(Path.of(source.source))
            path.parent.createDirectories()
            path.writer().use { it.write(source.initialContent) }
        }
    }

    private fun writeGradleProjectFiles() {
        getSettingsFile().writeText("")
        getBuildFile().writeText(
            """
            import dev.anies.gradle.template.TemplateTask
            plugins {
                id('dev.anies.gradle.template')
            }
            
            tasks.register("copyFile", Copy) {
                from('src/copy/')
                into('build/copy/')
            }
            
            tasks.register("testTemplating", TemplateTask) {
                engine = freemarker()
                data += [test: "template"]
                from('src/templates')
                into('build/templates')
                from(copyFile.outputs)
                rename("(.+).ftl", "\$1")
            }
            """.trimIndent()
        )
        getPropertiesFile().writeText("example=World!\n")
    }

    private fun runBuild(): BuildResult {
        val runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withProjectDir(getProjectDir())
        runner.withArguments("--stacktrace", "testTemplating")
        runner.withDebug(true)
        return runner.build()
    }

    private fun verifyResult(result: BuildResult, expectedFiles: List<ExpectedFile>) {
        println("Test project files:")
        getProjectDir().walkTopDown().forEach { println(it) }
        assertTrue(result.tasks.all { it.outcome == TaskOutcome.SUCCESS })
        assertTrue(getTemplateTestOutput().exists())
        for (expected in expectedFiles) {
            val finalPath = getProjectDir().toPath().resolve(Path.of(expected.dest))
            assertEquals(expected.expectedContent, finalPath.readText())
        }
    }
}

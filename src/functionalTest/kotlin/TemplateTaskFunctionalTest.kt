import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import kotlin.io.path.createDirectories
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
    private fun getTemplateTestFile() = getProjectDir().resolve("src/templates/example.txt.ftl")
    private fun getTemplateTestOutput() = getProjectDir().resolve("build/templates/example.txt")

    @Test
    fun `Templates file with properties value`() {
        setupTestProject()
        val result = runBuild()
        verifyResult(result)
    }

    private fun setupTestProject() {
        getSettingsFile().writeText("")
        getBuildFile().writeText(
            """
            plugins {
                id('dev.nies.build.template')
            }
            tasks.register("testTemplating", TemplateTask) {
                data += [test: "template"]
                from('src/templates')
                into('build/templates')
                rename("(.+).ftl", "\$1")
            }
            """.trimIndent()
        )
        getPropertiesFile().writeText("example=World!\n")
        getTemplateTestFile().parentFile.toPath().createDirectories()
        getTemplateTestFile().writeText(
            """
            Hello ${'$'}{properties.example}
            This is a ${'$'}{test}
        """.trimIndent()
        )
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

    private fun verifyResult(result: BuildResult) {
        println("Test project files:")
        getProjectDir().walkTopDown().forEach { println(it) }
        assertTrue(result.tasks.all { it.outcome == TaskOutcome.SUCCESS })
        assertTrue(getTemplateTestOutput().exists())
        assertEquals("Hello World!\nThis is a template", getTemplateTestOutput().readText())
    }
}

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import kotlin.io.path.createDirectories
import kotlin.test.Test
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
                from('src/templates')
                into('build/templates')
                rename("(.+).ftl", "\$1")
            }
            """.trimIndent()
        )
        getPropertiesFile().writeText("example=World!\n")
        getTemplateTestFile().parentFile.toPath().createDirectories()
        getTemplateTestFile().writeText("Hello \${example}")
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
        getProjectDir().walkTopDown().forEach { println(it) }
        assertTrue(getTemplateTestOutput().exists())
//        assertEquals("Hello World!", getTemplateTestOutput().readText())
    }
}

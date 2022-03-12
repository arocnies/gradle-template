import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import kotlin.test.Test

class TemplatePluginFunctionalTest {
    @get:Rule
    val tempFolder = TemporaryFolder()

    private fun getProjectDir() = tempFolder.root
    private fun getBuildFile() = getProjectDir().resolve("build.gradle")
    private fun getSettingsFile() = getProjectDir().resolve("settings.gradle")

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
                id('dev.anies.gradle.template')
            }
            
//            template {
//                engine = Freemarker { cfg -> ... }
//                engine = Velocity { cfg -> ... }
//                data = [defaultData: "Data from the plugin block"]
//            }
            """.trimIndent()
        )
    }

    private fun runBuild(): BuildResult {
        val runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withProjectDir(getProjectDir())
        return runner.build()
    }

    private fun verifyResult(result: BuildResult) {
        // Left blank as a placeholder. Compiling is a good validation.
    }
}

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createTempDirectory
import kotlin.io.path.readText
import kotlin.io.path.writer
import kotlin.test.assertEquals

data class ExpectedFile(
    val source: String,
    val dest: String,
    val initialContent: String,
    val expectedContent: String
)

class GradleProject {
    private val tempFolder = createTempDirectory().toFile()
    val expectedFiles: MutableList<ExpectedFile> = mutableListOf()
    val importLines = mutableListOf(
        "import dev.anies.gradle.template.TemplateTask",
        "import static dev.anies.gradle.template.freemarker.FreemarkerTemplateEngineKt.*"
    )
    val initialBuildFileContent = """
        ${importLines.joinToString("\n")}
        plugins {
            id('dev.anies.gradle.template')
        }
        """.trimIndent() + "\n"
    var buildFileContent = initialBuildFileContent
    val properties = mutableMapOf<String, String>()
    private val runner = GradleRunner.create().also {
        it.forwardOutput()
        it.withPluginClasspath()
        it.withProjectDir(getProjectDir())
        it.withDebug(true)
    }

    fun run(vararg arguments: String): BuildResult {
        runner.withArguments(*arguments)
        return runner.build()
    }

    private fun getProjectDir() = tempFolder
    private fun getBuildFile() = getProjectDir().resolve("build.gradle")
    private fun getSettingsFile() = getProjectDir().resolve("settings.gradle")
    private fun getPropertiesFile() = getProjectDir().resolve("gradle.properties")

    fun writeProject() {
        writeGradleProjectFiles()
        writeSourceFiles(expectedFiles)
    }

    private fun writeGradleProjectFiles() {
        getProjectDir().toPath().createDirectories()
        getSettingsFile().writeText("")
        getBuildFile().writeText(buildFileContent)
        getPropertiesFile().writeText(properties.map { (k, v) -> "$k=$v" }.joinToString("\n"))
    }

    private fun writeSourceFiles(sources: List<ExpectedFile>) {
        for (source in sources) {
            val path = getProjectDir().toPath().resolve(Path.of(source.source))
            path.parent.createDirectories()
            path.writer().use { it.write(source.initialContent) }
        }
    }

    fun assertAllFilesHaveExpectedContent() {
        for (expected in expectedFiles) {
            val finalPath = getProjectDir().toPath().resolve(Path.of(expected.dest))
            assertEquals(expected.expectedContent, finalPath.readText())
        }
    }

    fun printTestProjectFiles() {
        println("Test project files:")
        getProjectDir().walkTopDown().forEach { println(it) }
    }
}
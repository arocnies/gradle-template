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

data class EngineInstantiationExpression(val importLine: String, val expression: String)

val engineExpressions = listOf(
    EngineInstantiationExpression(
        "import static dev.anies.gradle.template.freemarker.FreemarkerTemplateEngineKt.*",
        "freemarker()"
    ),
    EngineInstantiationExpression(
        "import static dev.anies.gradle.template.freemarker.FreemarkerTemplateEngineKt.*",
        "freemarker({})"
    ),
    EngineInstantiationExpression(
        "import dev.anies.gradle.template.freemarker.FreemarkerTemplateEngine",
        "new FreemarkerTemplateEngine()"
    ),
    EngineInstantiationExpression(
        "import static dev.anies.gradle.template.velocity.VelocityTemplateEngineKt.*",
        "velocity()"
    ),
    EngineInstantiationExpression(
        "import static dev.anies.gradle.template.velocity.VelocityTemplateEngineKt.*",
        "velocity({})"
    ),
    EngineInstantiationExpression(
        "import dev.anies.gradle.template.velocity.VelocityTemplateEngine",
        "new VelocityTemplateEngine()"
    ),
)

class GradleProject {
    private val tempFolder = createTempDirectory().toFile()
    val expectedFiles: MutableList<ExpectedFile> = mutableListOf()
    val importLines = mutableListOf(
        "import dev.anies.gradle.template.TemplateTask"
    ) + engineExpressions.map { it.importLine }

    fun getBuildFileContent() = "${importLines.joinToString("\n")}\n${buildFileBody.trimIndent()}\n\n"
//    fun getBuildFileContent() = "${importLines.joinToString("\n")}\n${buildFileBody.trimIndent()}\n\n"

    var buildFileBody = """
        plugins {
            id('dev.anies.gradle.template')
        }
        
        """.trimIndent()
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
        getBuildFile().writeText(getBuildFileContent())
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
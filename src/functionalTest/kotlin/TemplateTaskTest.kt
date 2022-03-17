import org.gradle.testkit.runner.TaskOutcome
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.Test

@RunWith(Parameterized::class)
class TemplateTaskTest(private val templateEngine: String) {
    companion object {
        @Parameterized.Parameters(name = "{index}: Engine \"{0}\"")
        @JvmStatic
        fun getTemplateEngines() = listOf("freemarker()", "freemarker({})")
    }

    @Test
    fun `template plugin loads`() {
        val gradleProject = GradleProject()
        gradleProject.writeProject()
        val result = gradleProject.run("build")
        assert(result.tasks.all { it.outcome == TaskOutcome.SUCCESS })
    }

    @Test
    fun `templates file with project property`() {
        val gradleProject = GradleProject()
        gradleProject.expectedFiles += listOf(
            ExpectedFile(
                source = "src/templates/example.txt.ftl",
                dest = "build/templates/example.txt",
                initialContent = "Hello ${'$'}{properties.example}\nThis is a ${'$'}{test}",
                expectedContent = "Hello World!\nThis is a template"
            )
        )
        gradleProject.buildFileContent += """
            tasks.register("testTemplating", TemplateTask) {
                engine = ${templateEngine}
                data += [test: "template"]
                from('src/templates')
                into('build/templates')
                rename("(.+).ftl", "\${'$'}1")
            }
        """.trimIndent()
        gradleProject.properties += "example" to "World!"
        gradleProject.writeProject()
        val result = gradleProject.run("testTemplating", "--stacktrace")
        gradleProject.printTestProjectFiles()
        assert(result.tasks.all { it.outcome == TaskOutcome.SUCCESS })
        gradleProject.assertAllFilesHaveExpectedContent()
    }
}
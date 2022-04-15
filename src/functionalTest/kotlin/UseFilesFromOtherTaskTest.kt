import org.gradle.testkit.runner.TaskOutcome
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.Test

@RunWith(Parameterized::class)
class UseFilesFromOtherTaskTest(private val templateEngine: String) {
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
            ),
            ExpectedFile(
                source = "src/copy/taskFile.txt",
                dest = "build/templates/taskFile.txt",
                initialContent = "TaskFile ${'$'}{properties.example}\nThis is a ${'$'}{test}",
                expectedContent = "TaskFile World!\nThis is a template"
            )
        )
        gradleProject.buildFileBody += """
            tasks.register("copyFile", Copy) {
                from('src/copy/')
                into('build/copy/')
            }
            
            tasks.register("testTemplating", TemplateTask) {
                engine = $templateEngine
                data += [test: "template"]
                from('src/templates')
                into('build/templates')
                from(copyFile.outputs)
                rename("(.+).ftl", "\${'$'}1")
            }
        """.trimIndent()
        gradleProject.properties += "example" to "World!"
        gradleProject.writeProject()
        gradleProject.run("testTemplating", "--stacktrace").also { result ->
            gradleProject.assertAllFilesHaveExpectedContent()
            assert(result.tasks.all { it.outcome == TaskOutcome.SUCCESS })
        }
        gradleProject.run("testTemplating", "--stacktrace").also { result ->
            gradleProject.assertAllFilesHaveExpectedContent()
            assert(result.tasks.all { it.outcome == TaskOutcome.UP_TO_DATE })
        }
    }
}
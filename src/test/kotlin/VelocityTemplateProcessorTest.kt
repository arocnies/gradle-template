import dev.anies.gradle.template.velocity.VelocityTemplateProcessor
import org.gradle.testfixtures.ProjectBuilder
import java.io.File
import kotlin.io.path.readText
import kotlin.test.Test
import kotlin.test.assertEquals

class VelocityTemplateProcessorTest {
    @Test
    fun `Freemarker processor templates basic file`() {
        val processor = VelocityTemplateProcessor()
        val source = File(javaClass.getResource("velocity.template").toURI()).toPath()
        val dest = kotlin.io.path.createTempFile().toAbsolutePath()
        @Suppress("unused") val data = mapOf(
            "message" to "test-message",
            "obj" to object {
                val text = "obj-text"
                fun method() = "obj-method"
            }
        )
        val project = ProjectBuilder.builder().build()
        processor.load(project.fileTree(source))
        processor.processTemplate(source, dest, data)

        assertEquals(
            """
            Test message: test-message
            Test property objects: obj-text
            Test obj methods: obj-method
        """.trimIndent(),
            dest.readText()
                .replace("\r\n", "\n")
        )
    }
}
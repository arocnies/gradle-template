package dev.anies.gradle.template

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property

class TemplatePlugin : Plugin<Project> {
    private var data = mutableMapOf<String, Any?>()

    override fun apply(project: Project) {
        val extension = project.extensions.create("template", TemplatePluginExtension::class.java)

        project.tasks.register("template", TemplateTask::class.java) {
            it.data += this.data
            it.engine = extension.engine.get()
        }
    }
}

abstract class TemplatePluginExtension {
    abstract val engine: Property<TemplateEngineFactory<*, *>>
    abstract val data: Property<Map<String, Any?>>
}

open class TemplateEngineFactory<out T : TemplateEngine<C>, C>(
    val engineGenerator: () -> T,
    val config: C.() -> Unit
)

package dev.anies.gradle.template

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input

abstract class TemplatePluginExtension {
    @get:Input
    abstract val data: Property<Map<String, Any?>>
    abstract val engine: TemplateEngine<*>
}
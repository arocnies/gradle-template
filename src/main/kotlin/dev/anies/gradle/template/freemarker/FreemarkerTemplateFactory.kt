package dev.anies.gradle.template.freemarker

import dev.anies.gradle.template.TemplateEngineFactory
import dev.anies.gradle.template.TemplatePluginExtension
import freemarker.template.Configuration

class FreemarkerTemplateFactory(engineGenerator: () -> FreemarkerTemplateEngine, config: Configuration.() -> Unit) :
    TemplateEngineFactory<FreemarkerTemplateEngine, Configuration>(
        engineGenerator, config
    )

fun TemplatePluginExtension.freemarker(config: Configuration.() -> Unit = {}): FreemarkerTemplateFactory {
    return FreemarkerTemplateFactory(
        engineGenerator = { FreemarkerTemplateEngine() },
        config = config
    )
}

# Gradle Template Plugin

Template files during a gradle build.

## Usage

```groovy
plugins {
    id('dev.anies.gradle.template')
}

tasks.register("template", TemplateTask) {
    data += [key: "value"]
    from('src/templates')
    into('build/templates')
}
```

## Template Engines

Currently the only available engine is **Freemarker**, which is used by default.

Other engines are in progress with partial implementations completed.

### Engines:

- Freemarker: Available
- Velocity: Implemented & Unavailable
- Thymeleaf: Planned
- Pebble: Planned
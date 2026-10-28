/*
 * Copyright (c) Romanov Alexey, 2026
 */
package ru.romanow.feature.flags.config

import org.springframework.core.env.PropertiesPropertySource
import org.springframework.core.env.PropertySource
import org.springframework.core.io.support.EncodedResource
import org.springframework.core.io.support.PropertySourceFactory
import ru.romanow.feature.flags.services.readProperties

class FeaturesPropertySourceFactory : PropertySourceFactory {
    override fun createPropertySource(name: String?, resource: EncodedResource): PropertySource<*> =
        PropertiesPropertySource(name ?: resource.resource.description, readProperties(resource.resource.file.path))
}

/*
 * Copyright (c) Romanov Alexey, 2026
 */
package ru.romanow.feature.flags.services

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.FileUrlResource
import java.io.File
import java.util.*

fun readProperties(location: String?): Properties {
    val factory = YamlPropertiesFactoryBean()
    val defaultResource = ClassPathResource("features.yml")
    if (location != null && File(location).exists()) {
        factory.setResources(defaultResource, FileUrlResource(location))
    } else {
        factory.setResources(defaultResource)
    }
    return factory.`object`!!
}

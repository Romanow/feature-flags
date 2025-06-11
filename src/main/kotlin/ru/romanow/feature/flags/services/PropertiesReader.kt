/*
 * Copyright (c) Romanov Alexey, 2025
 */
package ru.romanow.feature.flags.services

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.FileUrlResource
import org.springframework.core.io.Resource
import java.io.File
import java.util.*

fun readProperties(location: String?): Properties {
    val factory = YamlPropertiesFactoryBean()
    val locations = mutableSetOf<Resource>(ClassPathResource("features.yml"))
    if (location != null && File(location).exists()) {
        locations.add(FileUrlResource(location))
    }
    factory.setResources(* locations.toTypedArray())
    return factory.`object`!!
}

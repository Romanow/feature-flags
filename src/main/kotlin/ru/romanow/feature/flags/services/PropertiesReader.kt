/*
 * Copyright (c) Romanov Alexey, 2024
 */
package ru.romanow.feature.flags.services

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.FileUrlResource
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import java.io.File
import java.util.*

@Service
class PropertiesReader {
    fun readProperties(externalConfigLocation: String?): Properties {
        val factory = YamlPropertiesFactoryBean()
        val locations = mutableSetOf<Resource>(ClassPathResource("features.yml"))
        if (externalConfigLocation != null && File(externalConfigLocation).exists()) {
            locations.add(FileUrlResource(externalConfigLocation))
        }
        factory.setResources(* locations.toTypedArray())
        return factory.`object`!!
    }
}

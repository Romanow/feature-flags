/*
 * Copyright (c) Romanov Alexey, 2024
 */
package ru.romanow.feature.flags.config

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean
import org.springframework.boot.SpringApplication
import org.springframework.boot.env.EnvironmentPostProcessor
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.PropertiesPropertySource
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.FileUrlResource
import org.springframework.core.io.Resource
import java.io.File

class FeaturesEnvironmentPostProcessor : EnvironmentPostProcessor {
    override fun postProcessEnvironment(environment: ConfigurableEnvironment, application: SpringApplication) {
        val configLocation = environment.getProperty("features.config.location")
        val factory = YamlPropertiesFactoryBean()
        val locations = mutableSetOf<Resource>(ClassPathResource("features.yml"))
        if (configLocation != null && File(configLocation).exists()) {
            locations.add(FileUrlResource(configLocation))
        }
        factory.setResources(* locations.toTypedArray())
        environment.propertySources.addLast(PropertiesPropertySource("features", factory.`object`!!))
    }
}

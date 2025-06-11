/*
 * Copyright (c) Romanov Alexey, 2025
 */
package ru.romanow.feature.flags.config

import org.springframework.boot.SpringApplication
import org.springframework.boot.env.EnvironmentPostProcessor
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.PropertiesPropertySource
import ru.romanow.feature.flags.services.readProperties

class FeaturesEnvironmentPostProcessor : EnvironmentPostProcessor {
    override fun postProcessEnvironment(environment: ConfigurableEnvironment, application: SpringApplication) {
        val configLocation = environment.getProperty("features.config.location")
        val properties = readProperties(configLocation)
        environment.propertySources.addLast(PropertiesPropertySource("features", properties))
    }
}

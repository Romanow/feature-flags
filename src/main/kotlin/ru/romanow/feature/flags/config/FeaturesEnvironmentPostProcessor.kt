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

class FeaturesEnvironmentPostProcessor : EnvironmentPostProcessor {
    override fun postProcessEnvironment(environment: ConfigurableEnvironment, application: SpringApplication) {
        val factory = YamlPropertiesFactoryBean()
        factory.setResources(ClassPathResource("features.yml"))
        environment.propertySources.addLast(PropertiesPropertySource("features", factory.`object`!!))
    }
}

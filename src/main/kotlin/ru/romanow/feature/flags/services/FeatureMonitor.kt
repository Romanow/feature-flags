/*
 * Copyright (c) Romanov Alexey, 2025
 */
package ru.romanow.feature.flags.services

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.EnvironmentAware
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.Environment
import org.springframework.core.env.PropertiesPropertySource
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*

@Component
class FeatureMonitor : EnvironmentAware, ApplicationContextAware {
    private val logger = LoggerFactory.getLogger(FeatureMonitor::class.java)

    @Value("\${features.config.location:}")
    private val configLocation: String? = null
    private lateinit var environment: ConfigurableEnvironment
    private lateinit var beanFactory: ConfigurableListableBeanFactory

    @Scheduled(fixedDelayString = "\${features.config.reload-interval}", initialDelayString = "PT5S")
    fun refresh() {
        val existingProperties = environment.propertySources["features"].source as Properties
        val newProperties = readProperties(configLocation)
        if (existingProperties != newProperties) {
            logger.info("Properties changed, reloading...")
            environment.propertySources.addLast(PropertiesPropertySource("features", newProperties))
        }
    }

    override fun setEnvironment(environment: Environment) {
        this.environment = environment as ConfigurableEnvironment
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.beanFactory = (applicationContext as ConfigurableApplicationContext).beanFactory
    }
}

/*
 * Copyright (c) Romanov Alexey, 2024
 */
package ru.romanow.feature.flags.services

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.support.DefaultSingletonBeanRegistry
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.EnvironmentAware
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.Environment
import org.springframework.core.env.PropertiesPropertySource
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import ru.romanow.feature.flags.config.FeatureBeans
import java.util.*

@Component
class FeatureMonitor(
    private val reader: PropertiesReader,
    private val featureBeans: FeatureBeans
) : EnvironmentAware, ApplicationContextAware {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Value("\${features.config.location:}")
    private val configLocation: String? = null
    private lateinit var environment: ConfigurableEnvironment
    private lateinit var context: ConfigurableApplicationContext

    @Scheduled(fixedDelayString = "\${features.config.reload-interval}", initialDelayString = "PT5S")
    fun refresh() {
        val existingProperties = environment.propertySources["features"].source as Properties
        val newProperties = reader.readProperties(configLocation)
        if (existingProperties != newProperties) {
            logger.info("Properties changed, reloading...")
            environment.propertySources.addLast(PropertiesPropertySource("features", newProperties))
            val beanFactory = context.autowireCapableBeanFactory as DefaultSingletonBeanRegistry
            for (name in featureBeans.keys) {
                beanFactory.destroySingleton(name)
            }
        }
    }

    override fun setEnvironment(environment: Environment) {
        this.environment = environment as ConfigurableEnvironment
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.context = applicationContext as ConfigurableApplicationContext
    }
}

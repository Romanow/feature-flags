/*
 * Copyright (c) Romanov Alexey, 2026
 */
package ru.romanow.feature.flags.services

import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.bind.Bindable
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.PropertiesPropertySource
import ru.romanow.feature.flags.config.FeatureFieldBeanPostProcessor
import ru.romanow.feature.flags.config.FeatureImplementationBeanFactoryPostProcessor
import ru.romanow.feature.flags.properties.FeatureConfigProperties
import ru.romanow.feature.flags.properties.Features
import java.util.*

class FeatureMonitor(
    private val featureFieldBeanPostProcessor: FeatureFieldBeanPostProcessor,
    private val features: Features,
    private val properties: FeatureConfigProperties,
    private val environment: ConfigurableEnvironment,
    private val implementationBeanFactoryPostProcessor: FeatureImplementationBeanFactoryPostProcessor
) {
    private val logger = LoggerFactory.getLogger(FeatureMonitor::class.java)

    @Synchronized
    fun refresh() {
        val existingSource = environment.propertySources["features"]!!
        val existingProperties = existingSource.source as Properties
        val newProperties = readProperties(properties.location)
        if (existingProperties != newProperties) {
            logger.info("Properties changed, reloading...")
            environment.propertySources.replace("features", PropertiesPropertySource("features", newProperties))
            try {
                val updatedFeatures = Binder.get(environment)
                    .bind("features", Bindable.of(Features::class.java))
                    .orElseGet { Features() }

                features.clear()
                features.putAll(updatedFeatures)

                featureFieldBeanPostProcessor.refresh()
                implementationBeanFactoryPostProcessor.refresh()
            } catch (exception: RuntimeException) {
                environment.propertySources.replace("features", existingSource)
                throw exception
            }
        }
    }
}

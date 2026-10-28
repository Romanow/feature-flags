/*
 * Copyright (c) Romanov Alexey, 2026
 */
package ru.romanow.feature.flags

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.PropertySource
import org.springframework.context.annotation.PropertySources
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.Environment
import org.springframework.scheduling.config.ContextLifecycleScheduledTaskRegistrar
import org.springframework.scheduling.config.FixedDelayTask
import ru.romanow.feature.flags.config.FeatureFieldBeanPostProcessor
import ru.romanow.feature.flags.config.FeatureImplementationBeanFactoryPostProcessor
import ru.romanow.feature.flags.config.FeaturesPropertySourceFactory
import ru.romanow.feature.flags.config.YamlPropertySourceFactory
import ru.romanow.feature.flags.properties.FeatureConfigProperties
import ru.romanow.feature.flags.properties.Features
import ru.romanow.feature.flags.services.FeatureMonitor

@AutoConfiguration
@ConditionalOnProperty(prefix = "features-config", name = ["enabled"], havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(Features::class, FeatureConfigProperties::class)
@PropertySources(
    value = [
        PropertySource(
            name = "features-config",
            value = ["classpath:features-config.yml"],
            factory = YamlPropertySourceFactory::class
        ),
        PropertySource(
            name = "features",
            value = [$$"file:${features-config.location}"],
            factory = FeaturesPropertySourceFactory::class
        )
    ]
)
class FeatureFlagsAutoConfiguration {

    @Bean
    fun featureMonitor(
        processor: FeatureFieldBeanPostProcessor,
        features: Features,
        properties: FeatureConfigProperties,
        environment: ConfigurableEnvironment,
        implementations: FeatureImplementationBeanFactoryPostProcessor
    ) = FeatureMonitor(processor, features, properties, environment, implementations)

    @Bean
    @ConditionalOnProperty(
        prefix = "features-config", name = ["reload-enabled"], havingValue = "true", matchIfMissing = true
    )
    fun featureReloadTasks(monitor: FeatureMonitor, properties: FeatureConfigProperties) =
        ContextLifecycleScheduledTaskRegistrar().apply {
            addFixedDelayTask(FixedDelayTask(monitor::refresh, properties.reloadInterval, properties.reloadInitDelay))
        }

    companion object {
        @Bean
        @JvmStatic
        fun featureImplementationBeanFactoryPostProcessor(environment: Environment) =
            FeatureImplementationBeanFactoryPostProcessor(environment)

        @Bean
        @JvmStatic
        fun featureFieldBeanPostProcessor(environment: Environment) = FeatureFieldBeanPostProcessor(environment)
    }
}

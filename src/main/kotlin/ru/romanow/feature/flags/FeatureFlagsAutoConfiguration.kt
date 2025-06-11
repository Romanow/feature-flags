/*
 * Copyright (c) Romanov Alexey, 2025
 */
package ru.romanow.feature.flags

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.core.env.Environment
import org.springframework.scheduling.annotation.EnableScheduling
import ru.romanow.feature.flags.config.FeatureBeanFactoryPostProcessor
import ru.romanow.feature.flags.properties.Features

@EnableScheduling
@AutoConfiguration
@EnableConfigurationProperties(Features::class)
@ComponentScan(basePackages = ["ru.romanow.feature.flags"])
class FeatureFlagsAutoConfiguration {

    @Bean
    fun featureBeanFactoryPostProcessor(environment: Environment, applicationContext: ApplicationContext) =
        FeatureBeanFactoryPostProcessor(environment, applicationContext)
}

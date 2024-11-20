/*
 * Copyright (c) Romanov Alexey, 2024
 */
package ru.romanow.feature.flags

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import ru.romanow.feature.flags.config.FeatureBeanPostProcessor
import ru.romanow.feature.flags.properties.Features

@AutoConfiguration
@EnableConfigurationProperties(Features::class)
class FeatureFlagsAutoConfiguration {

    @Bean
    fun featureBeanPostProcessor(features: Features) = FeatureBeanPostProcessor(features)
}

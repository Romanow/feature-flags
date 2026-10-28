/*
 * Copyright (c) Romanov Alexey, 2026
 */
package ru.romanow.feature.flags.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "features-config")
data class FeatureConfigProperties(
    val enabled: Boolean,
    val reloadEnabled: Boolean,
    val location: String,
    val reloadInterval: Duration,
    val reloadInitDelay: Duration
)

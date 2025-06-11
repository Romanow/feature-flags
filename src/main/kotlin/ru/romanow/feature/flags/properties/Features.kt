/*
 * Copyright (c) Romanov Alexey, 2025
 */
package ru.romanow.feature.flags.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "features")
class Features : LinkedHashMap<String, String>()

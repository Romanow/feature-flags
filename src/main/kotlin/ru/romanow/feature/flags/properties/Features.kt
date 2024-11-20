/*
 * Copyright (c) Romanov Alexey, 2024
 */
package ru.romanow.feature.flags.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "features")
class Features : LinkedHashMap<String, String>()

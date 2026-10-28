/*
 * Copyright (c) Romanov Alexey, 2026
 */
package ru.romanow.feature.flags.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import java.util.concurrent.ConcurrentHashMap

@ConfigurationProperties(prefix = "features")
class Features : ConcurrentHashMap<String, String>()

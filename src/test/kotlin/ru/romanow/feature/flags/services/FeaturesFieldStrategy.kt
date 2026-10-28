/*
 * Copyright (c) Romanov Alexey, 2026
 */
package ru.romanow.feature.flags.services

import org.springframework.stereotype.Component
import ru.romanow.feature.flags.properties.Features

@Component
class FeaturesFieldStrategy(private val features: Features) : Strategy {
    override fun produce() = "Feature value: ${features["answer"]}"
}

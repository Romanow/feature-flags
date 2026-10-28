/*
 * Copyright (c) Romanov Alexey, 2026
 */
package ru.romanow.feature.flags.services

import org.springframework.stereotype.Component
import ru.romanow.feature.flags.annotations.Feature

@Component
class FeatureAnnotationStrategy : Strategy {
    @Feature("answer")
    private var answer: String? = null

    override fun produce() = "Feature value: $answer"
}

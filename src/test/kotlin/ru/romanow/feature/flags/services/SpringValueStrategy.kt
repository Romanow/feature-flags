/*
 * Copyright (c) Romanov Alexey, 2026
 */
package ru.romanow.feature.flags.services

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class SpringValueStrategy : Strategy {

    @Value($$"${features.answer}")
    private var answer: Int? = null

    override fun produce() = "Feature value: $answer"
}

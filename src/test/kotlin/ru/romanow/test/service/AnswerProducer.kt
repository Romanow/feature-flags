/*
 * Copyright (c) Romanov Alexey, 2024
 */
package ru.romanow.test.service

import org.springframework.stereotype.Service
import ru.romanow.feature.flags.annotations.ConditionOnFeatureEnabled
import ru.romanow.feature.flags.annotations.DefaultFeatureImplementation

interface AnswerProducer {
    fun response(): String
}

@Service
@ConditionOnFeatureEnabled("answer")
class MainAnswerProducer : AnswerProducer {
    override fun response() = "42"
}

@Service
@DefaultFeatureImplementation("answer")
class DefaultAnswerProducer : AnswerProducer {
    override fun response() = "Hello, world"
}

/*
 * Copyright (c) Romanov Alexey, 2026
 */
package ru.romanow.feature.flags.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import ru.romanow.feature.flags.annotations.ConditionOnFeatureEnabled
import ru.romanow.feature.flags.annotations.DefaultFeatureImplementation

@Component
class ConditionalAnnotationStrategy : Strategy {

    @Autowired
    private lateinit var answerProducer: AnswerProducer

    override fun produce() = "Feature value: ${answerProducer.response()}"
}

interface AnswerProducer {
    fun response(): String
}

@Service
@ConditionOnFeatureEnabled(feature = "answer", expected = "42")
class MainAnswerProducer : AnswerProducer {
    override fun response() = "42"
}

@Service
@ConditionOnFeatureEnabled(feature = "answer", expected = "1")
class SecondaryAnswerProducer : AnswerProducer {
    override fun response() = "1"
}

@Service
@DefaultFeatureImplementation(feature = "answer")
class DefaultAnswerProducer : AnswerProducer {
    override fun response() = "0"
}

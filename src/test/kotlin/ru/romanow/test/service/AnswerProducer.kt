/*
 * Copyright (c) Romanov Alexey, 2024
 */
package ru.romanow.test.service

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode.TARGET_CLASS
import org.springframework.stereotype.Service
import ru.romanow.feature.flags.annotations.ConditionOnFeatureEnabled
import ru.romanow.feature.flags.annotations.DefaultFeatureImplementation

interface AnswerProducer {
    fun response(): String
}

@Lazy
@Scope(proxyMode = TARGET_CLASS)
@Service
@ConditionOnFeatureEnabled("answer")
class MainAnswerProducer : AnswerProducer {
    private val logger = LoggerFactory.getLogger(AnswerProducer::class.java)

    @PostConstruct
    fun init() {
        logger.info("Init MainAnswerProducer")
    }

    override fun response() = "42"
}

@Lazy
@Scope(proxyMode = TARGET_CLASS)
@Service
@DefaultFeatureImplementation("answer")
class DefaultAnswerProducer : AnswerProducer {
    private val logger = LoggerFactory.getLogger(AnswerProducer::class.java)

    @PostConstruct
    fun init() {
        logger.info("Init DefaultAnswerProducer")
    }

    override fun response() = "Hello, world"
}

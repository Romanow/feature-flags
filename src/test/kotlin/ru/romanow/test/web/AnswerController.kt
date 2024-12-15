/*
 * Copyright (c) Romanov Alexey, 2024
 */
package ru.romanow.test.web

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import ru.romanow.feature.flags.annotations.Feature
import ru.romanow.test.service.AnswerProducer

@RestController
class AnswerController(private val producer: AnswerProducer) {

    private val logger = LoggerFactory.getLogger(AnswerController::class.java)

    @Feature(value = "answer")
    private var answer: Boolean? = null

    @PostConstruct
    fun init() {
        logger.info("Init AnswerController")
    }

    @GetMapping
    fun answer() = "Response: ${producer.response()} (feature flag: $answer)"
}

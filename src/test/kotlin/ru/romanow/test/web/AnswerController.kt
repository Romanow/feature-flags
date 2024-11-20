/*
 * Copyright (c) Romanov Alexey, 2024
 */
package ru.romanow.test.web

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import ru.romanow.feature.flags.annotations.Feature
import ru.romanow.test.service.AnswerProducer

@RestController
class AnswerController(private val producer: AnswerProducer) {

    @Feature(value = "answer")
    private var answer: Boolean? = null

    @GetMapping
    fun answer() = "Response: ${producer.response()} (feature flag: $answer)"
}

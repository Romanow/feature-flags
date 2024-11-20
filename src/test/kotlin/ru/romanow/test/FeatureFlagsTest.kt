/*
 * Copyright (c) Romanov Alexey, 2024
 */
package ru.romanow.test

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import ru.romanow.feature.flags.properties.Features

@SpringBootTest
@AutoConfigureMockMvc
internal class FeatureFlagsTest {

    @Autowired
    private lateinit var features: Features

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun test() {
        assertThat(features["answer"]).isEqualTo("true")

        mockMvc.get("/")
            .andExpect {
                status { isOk() }
                content { string("Response: 42 (feature flag: true)") }
            }
    }

    @SpringBootApplication
    internal class FeatureFlagsTestApplication
}

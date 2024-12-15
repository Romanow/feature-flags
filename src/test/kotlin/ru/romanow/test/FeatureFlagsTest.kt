/*
 * Copyright (c) Romanov Alexey, 2024
 */
package ru.romanow.test

import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import ru.romanow.feature.flags.services.PropertiesReader
import java.util.*

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
internal class FeatureFlagsTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Value("\${features.config.location:}")
    private val configLocation: String? = null

    @MockBean
    private lateinit var reader: PropertiesReader

    @Test
    fun test() {
        mockMvc.get("/")
            .andExpect {
                status { isOk() }
                content { string("Response: Hello, world (feature flag: false)") }
            }

        val properties = Properties()
        properties.putAll(listOf(Pair("features.answer", "true"), Pair("features.updated", "true")))
        `when`(reader.readProperties(configLocation)).thenReturn(properties)

        Thread.sleep(10000)

        mockMvc.get("/")
            .andExpect {
                status { isOk() }
                content { string("Response: 42 (feature flag: true)") }
            }
    }

    @SpringBootApplication
    internal class FeatureFlagsTestApplication
}

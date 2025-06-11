/*
 * Copyright (c) Romanov Alexey, 2025
 */
package ru.romanow.test

import io.mockk.every
import io.mockk.mockkStatic
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import ru.romanow.feature.flags.services.readProperties
import java.util.*

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
internal class FeatureFlagsTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Value("\${features.config.location:}")
    private val configLocation: String? = null

    @Test
    fun test() {
        mockMvc.get("/")
            .andExpect {
                status { isOk() }
                content { string("Response: Hello, world (feature flag: null)") } // TODO false
            }

        val properties = Properties()
        properties += Pair("features.answer", "true")
        properties += Pair("features.updated", "true")

        mockkStatic(::readProperties)
        every { readProperties(configLocation) } returns properties

        Thread.sleep(10000)

        mockMvc.get("/")
            .andExpect {
                status { isOk() }
                content { string("Response: 42 (feature flag: null)") } // TODO true
            }
    }

    @SpringBootApplication
    internal class FeatureFlagsTestApplication
}

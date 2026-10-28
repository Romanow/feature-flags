/*
 * Copyright (c) Romanov Alexey, 2026
 */
package ru.romanow.feature.flags

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.context.SpringBootTest
import ru.romanow.feature.flags.services.FeatureMonitor
import ru.romanow.feature.flags.services.Strategy
import ru.romanow.feature.flags.services.readProperties
import java.util.*

@SpringBootTest(properties = ["features-config.reload-enabled=false"])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class FeatureFlagsTest {

    @Autowired
    private lateinit var featureMonitor: FeatureMonitor

    @Value($$"${features-config.location:}")
    private val configLocation: String? = null

    @Autowired
    private lateinit var strategies: List<Strategy>

    @ParameterizedTest
    @MethodSource("strategies")
    fun test(strategy: Strategy) {
        assertThat(strategy.produce()).isEqualTo("Feature value: 0")
        refreshWith("answer" to "42")
        assertThat(strategy.produce()).isEqualTo("Feature value: 42")
    }

    fun strategies(): List<Strategy> = strategies

    @AfterEach
    fun restoreProperties() {
        unmockkStatic(::readProperties)
        featureMonitor.refresh()
    }

    private fun refreshWith(vararg values: Pair<String, String>) {
        val properties = Properties()
        values.forEach { (key, value) -> properties.setProperty("features.$key", value) }
        mockkStatic(::readProperties)
        every { readProperties(configLocation) } returns properties
        featureMonitor.refresh()
    }

    @SpringBootApplication
    internal class FeatureFlagsTestApplication
}

/*
 * Copyright (c) Romanov Alexey, 2026
 */
package ru.romanow.feature.flags.config

import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.MutablePropertyValues
import org.springframework.beans.TypeMismatchException
import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.support.DefaultListableBeanFactory
import org.springframework.mock.env.MockEnvironment
import ru.romanow.feature.flags.annotations.Feature

internal class FeatureFieldBeanPostProcessorTest {

    private val environment = MockEnvironment()
    private val processor = FeatureFieldBeanPostProcessor(environment)
        .apply { setBeanFactory(DefaultListableBeanFactory()) }

    @Test
    fun `reject final feature field`() {
        assertInvalid(FinalFeature(), "must be mutable and non-static")
    }

    @Test
    fun `reject static feature field`() {
        assertInvalid(StaticFeature, "must be mutable and non-static")
    }

    @Test
    fun `reject final value field`() {
        assertInvalid(FinalValue(), "must be mutable and non-static")
    }

    @Test
    fun `reject blank feature name`() {
        assertInvalid(BlankFeature(), "non-blank feature")
    }

    @Test
    fun `reject conflicting field annotations`() {
        assertInvalid(ConflictingAnnotations(), "must not declare both @Feature and @Value")
    }

    @Test
    fun `resolve feature and preserve previous values when refresh fails`() {
        val bean = ValidFeatures()
        environment.withProperty("features.first", "10").withProperty("features.second", "20")
        processor.postProcessProperties(MutablePropertyValues(), bean, "features")
        assertThat(bean.first).isEqualTo(10)
        assertThat(bean.second).isEqualTo(20)

        environment.withProperty("features.first", "30").withProperty("features.second", "40")
        processor.refresh()
        assertThat(bean.first).isEqualTo(30)
        assertThat(bean.second).isEqualTo(40)

        environment.withProperty("features.first", "10").withProperty("features.second", "invalid")
        assertThatThrownBy { processor.refresh() }.isInstanceOf(TypeMismatchException::class.java)
        assertThat(bean.first).isEqualTo(30)
        assertThat(bean.second).isEqualTo(40)
    }

    private fun assertInvalid(bean: Any, message: String) {
        assertThatIllegalArgumentException()
            .isThrownBy { processor.postProcessProperties(MutablePropertyValues(), bean, "invalid") }
            .withMessageContaining(message)
    }

    private class FinalFeature {
        @Feature("answer")
        val answer = 0
    }

    private object StaticFeature {
        @JvmField
        @Feature("answer")
        var answer = 0
    }

    private class FinalValue {
        @Value($$"${features.answer}")
        val answer = 0
    }

    private class BlankFeature {
        @Feature(" ")
        var answer = 0
    }

    private class ConflictingAnnotations {
        @Feature("answer")
        @Value($$"${features.answer}")
        var answer = 0
    }

    private class ValidFeatures {
        @Feature("first")
        var first = 0

        @Feature("second")
        var second = 0
    }
}

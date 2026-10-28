/*
 * Copyright (c) Romanov Alexey, 2026
 */
package ru.romanow.feature.flags.config

import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.support.ChildBeanDefinition
import org.springframework.beans.factory.support.DefaultListableBeanFactory
import org.springframework.beans.factory.support.RootBeanDefinition
import org.springframework.mock.env.MockEnvironment
import ru.romanow.feature.flags.annotations.ConditionOnFeatureEnabled
import ru.romanow.feature.flags.annotations.DefaultFeatureImplementation

internal class FeatureImplementationBeanFactoryPostProcessorTest {

    private val environment = MockEnvironment()
    private val factory = DefaultListableBeanFactory()
    private val processor = FeatureImplementationBeanFactoryPostProcessor(environment)

    @Test
    fun `reject conflicting implementation annotations`() {
        assertInvalid("must not declare both", ConflictingAnnotations::class.java)
    }

    @Test
    fun `reject blank feature name`() {
        assertInvalid("non-blank feature", BlankFeature::class.java)
        assertInvalid("non-blank feature", BlankDefaultFeature::class.java)
    }

    @Test
    fun `reject multiple default implementations`() {
        assertInvalid("Multiple default implementations", Fallback::class.java, OtherFallback::class.java)
    }

    @Test
    fun `reject duplicate conditions even when feature is inactive`() {
        assertInvalid("Multiple implementations", Enabled::class.java, OtherEnabled::class.java)
    }

    @Test
    fun `reject implementations without a common interface`() {
        assertInvalid("must share at least one business interface", Enabled::class.java, UnrelatedFallback::class.java)
    }

    @Test
    fun `reject implementation without interfaces`() {
        assertInvalid("must share at least one business interface", UnrelatedFallback::class.java)
    }

    @Test
    fun `reject implementations sharing only inherited lifecycle interfaces`() {
        assertInvalid(
            "must share at least one business interface", LifecycleEnabled::class.java, LifecycleFallback::class.java
        )
    }

    @ParameterizedTest
    @ValueSource(strings = ["prototype", "request", "session", "custom"])
    fun `reject non-singleton implementations even when inactive`(scope: String) {
        listOf(Enabled::class.java, Fallback::class.java).forEach { type ->
            val invalidFactory = DefaultListableBeanFactory()
            val beanName = type.simpleName
            invalidFactory.registerBeanDefinition(beanName, RootBeanDefinition(type).apply { this.scope = scope })

            assertThatIllegalArgumentException()
                .isThrownBy { processor.postProcessBeanFactory(invalidFactory) }
                .withMessageContainingAll("bean '$beanName'", "feature 'answer'", "singleton scope", "actual: '$scope'")
            assertThat(invalidFactory.beanDefinitionNames).containsExactly(beanName)
        }
    }

    @Test
    fun `reject inherited non-singleton scope`() {
        factory.registerBeanDefinition(
            "parent",
            RootBeanDefinition(Any::class.java).apply {
                isAbstract = true
                scope = "prototype"
            }
        )
        factory.registerBeanDefinition(
            "fallback",
            ChildBeanDefinition("parent").apply { beanClassName = Fallback::class.java.name }
        )

        assertThatIllegalArgumentException()
            .isThrownBy { processor.postProcessBeanFactory(factory) }
            .withMessageContainingAll("bean 'fallback'", "singleton scope", "actual: 'prototype'")
        assertThat(factory.beanDefinitionNames).containsExactly("parent", "fallback")
    }

    @Test
    fun `accept explicit singleton scope`() {
        val definition = RootBeanDefinition(Fallback::class.java).apply { scope = "singleton" }
        factory.registerBeanDefinition("fallback", definition)

        processor.postProcessBeanFactory(factory)

        assertThat(factory.getBean(Answer::class.java).answer()).isEqualTo("default")
    }

    @Test
    fun `reject missing implementation during proxy initialization`() {
        register(Enabled::class.java)
        processor.postProcessBeanFactory(factory)

        assertThatThrownBy { factory.getBean(Answer::class.java) }
            .hasRootCauseInstanceOf(IllegalArgumentException::class.java)
            .hasStackTraceContaining("No implementation matches feature 'answer'")
    }

    @Test
    fun `preserve current implementation when refresh has no match`() {
        environment.withProperty("features.answer", "true")
        register(Enabled::class.java)
        processor.postProcessBeanFactory(factory)
        val proxy = factory.getBean(Answer::class.java)
        assertThat(proxy.answer()).isEqualTo("enabled")

        environment.withProperty("features.answer", "false")
        assertThatIllegalArgumentException().isThrownBy { processor.refresh() }
            .withMessageContaining("No implementation matches")
        assertThat(proxy.answer()).isEqualTo("enabled")
    }

    @Test
    fun `switch between matching and default implementations`() {
        assertSwitchingImplementations(Enabled::class.java, Fallback::class.java)
    }

    @Test
    fun `switch between direct and inherited implementations of a common interface`() {
        assertSwitchingImplementations(Enabled::class.java, ExtendedFallback::class.java)
    }

    @Test
    fun `switch between implementations of different child interfaces`() {
        assertSwitchingImplementations(ExtendedEnabled::class.java, ExtendedFallback::class.java)
    }

    @Test
    fun `switch between implementations inheriting interfaces through a base class`() {
        assertSwitchingImplementations(InheritedEnabled::class.java, Fallback::class.java)
    }

    private fun assertSwitchingImplementations(enabled: Class<*>, fallback: Class<*>) {
        register(enabled, fallback)
        processor.postProcessBeanFactory(factory)
        val proxy = factory.getBean(Answer::class.java)
        assertThat(proxy.answer()).isEqualTo("default")

        environment.withProperty("features.answer", "true")
        processor.refresh()
        assertThat(proxy.answer()).isEqualTo("enabled")

        environment.withProperty("features.answer", "false")
        processor.refresh()
        assertThat(proxy.answer()).isEqualTo("default")
    }

    private fun assertInvalid(message: String, vararg types: Class<*>) {
        val invalidFactory = DefaultListableBeanFactory()
        types.forEach { invalidFactory.registerBeanDefinition(it.simpleName, RootBeanDefinition(it)) }
        assertThatIllegalArgumentException()
            .isThrownBy { processor.postProcessBeanFactory(invalidFactory) }
            .withMessageContaining(message)
        assertThat(invalidFactory.beanDefinitionNames).containsExactly(*types.map { it.simpleName }.toTypedArray())
    }

    private fun register(vararg types: Class<*>) {
        types.forEach { factory.registerBeanDefinition(it.simpleName, RootBeanDefinition(it)) }
    }

    interface Answer {
        fun answer(): String
    }

    interface ExtendedAnswer : Answer

    interface DeepExtendedAnswer : ExtendedAnswer

    interface OtherExtendedAnswer : Answer

    interface EnabledLifecycle : InitializingBean

    interface FallbackLifecycle : InitializingBean

    @ConditionOnFeatureEnabled("answer")
    class ExtendedEnabled : DeepExtendedAnswer {
        override fun answer() = "enabled"
    }

    @DefaultFeatureImplementation("answer")
    class ExtendedFallback : OtherExtendedAnswer {
        override fun answer() = "default"
    }

    open class BaseAnswer : DeepExtendedAnswer {
        override fun answer() = "enabled"
    }

    @ConditionOnFeatureEnabled("answer")
    class InheritedEnabled : BaseAnswer()

    @ConditionOnFeatureEnabled("answer")
    class LifecycleEnabled : EnabledLifecycle {
        override fun afterPropertiesSet() = Unit
    }

    @DefaultFeatureImplementation("answer")
    class LifecycleFallback : FallbackLifecycle {
        override fun afterPropertiesSet() = Unit
    }

    @ConditionOnFeatureEnabled("answer")
    class Enabled : Answer {
        override fun answer() = "enabled"
    }

    @ConditionOnFeatureEnabled("answer")
    class OtherEnabled : Answer {
        override fun answer() = "other"
    }

    @DefaultFeatureImplementation("answer")
    class Fallback : Answer {
        override fun answer() = "default"
    }

    @DefaultFeatureImplementation("answer")
    class OtherFallback : Answer {
        override fun answer() = "other default"
    }

    @DefaultFeatureImplementation("answer")
    class UnrelatedFallback

    @ConditionOnFeatureEnabled("answer")
    @DefaultFeatureImplementation("answer")
    class ConflictingAnnotations

    @ConditionOnFeatureEnabled("")
    class BlankFeature

    @DefaultFeatureImplementation("")
    class BlankDefaultFeature
}

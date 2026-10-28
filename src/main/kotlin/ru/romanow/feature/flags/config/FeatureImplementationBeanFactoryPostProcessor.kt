/*
 * Copyright (c) Romanov Alexey, 2026
 */
package ru.romanow.feature.flags.config

import org.springframework.aop.support.AopUtils
import org.springframework.beans.factory.Aware
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.SmartInitializingSingleton
import org.springframework.beans.factory.config.BeanFactoryPostProcessor
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.DefaultListableBeanFactory
import org.springframework.beans.factory.support.RootBeanDefinition
import org.springframework.context.Lifecycle
import org.springframework.context.Phased
import org.springframework.core.env.Environment
import org.springframework.util.ClassUtils.getAllInterfacesForClassAsSet
import ru.romanow.feature.flags.annotations.ConditionOnFeatureEnabled
import ru.romanow.feature.flags.annotations.DefaultFeatureImplementation
import ru.romanow.feature.flags.models.Candidate
import ru.romanow.feature.flags.models.ImplementationGroup
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Proxy
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

class FeatureImplementationBeanFactoryPostProcessor(private val environment: Environment) : BeanFactoryPostProcessor {

    private lateinit var beanFactory: DefaultListableBeanFactory
    private val groups = mutableListOf<ImplementationGroup>()
    private val lock = ReentrantReadWriteLock()
    private val infrastructureInterfaces = listOf(
        Aware::class.java,
        InitializingBean::class.java,
        DisposableBean::class.java,
        SmartInitializingSingleton::class.java,
        Lifecycle::class.java,
        Phased::class.java,
        AutoCloseable::class.java
    )

    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
        this.beanFactory = beanFactory as DefaultListableBeanFactory

        val candidates = beanFactory.beanDefinitionNames.mapNotNull { beanName ->
            val condition = beanFactory.findAnnotationOnBean(beanName, ConditionOnFeatureEnabled::class.java, false)
            val fallback = beanFactory.findAnnotationOnBean(beanName, DefaultFeatureImplementation::class.java, false)
            val feature = condition?.feature ?: fallback?.feature ?: return@mapNotNull null

            require(condition == null || fallback == null) {
                "Bean '$beanName' must not declare both @ConditionOnFeatureEnabled and @DefaultFeatureImplementation"
            }
            require(feature.isNotBlank()) {
                "Bean '$beanName' must specify a non-blank feature"
            }

            val definition = beanFactory.getBeanDefinition(beanName)
            val mergedDefinition = beanFactory.getMergedBeanDefinition(beanName)

            require(mergedDefinition.isSingleton) {
                "Feature implementation bean '$beanName' for feature '$feature' must have singleton scope " +
                    "(actual: '${mergedDefinition.scope}')"
            }

            val beanType = beanFactory.getType(beanName, false)
            return@mapNotNull Candidate(
                beanName = beanName,
                definition = definition,
                feature = feature,
                condition = condition?.expected,
                type = beanType!!
            )
        }

        candidates.groupBy { it.feature }
            .map { (feature, implementations) -> ImplementationGroup(feature, implementations) }
            .onEach { validateGroup(it) }
            .forEach { group ->
                registerGroup(group, this.beanFactory)
                groups += group
            }
    }

    fun refresh() = lock.write {
        val replacements = groups.associateWith { select(it) }
        replacements.forEach { (group, target) -> group.current = target }
    }

    private fun registerGroup(group: ImplementationGroup, registry: BeanDefinitionRegistry) {
        val interfaces = commonBusinessInterfaces(group)
        val handler = InvocationHandler { instance, method, args ->
            when {
                method.declaringClass == Any::class.java -> when (method.name) {
                    "equals" -> instance === args?.get(0)
                    "hashCode" -> System.identityHashCode(instance)
                    else -> "Feature implementation proxy: ${group.feature}"
                }

                else -> lock.read {
                    AopUtils.invokeJoinpointUsingReflection(group.current, method, args ?: emptyArray())
                }
            }
        }
        val proxy = Proxy.newProxyInstance(beanFactory.beanClassLoader, interfaces.toTypedArray(), handler)
        group.candidates.forEach { candidate ->
            registry.removeBeanDefinition(candidate.beanName)
            candidate.definition.isLazyInit = true
            candidate.definition.isAutowireCandidate = false
            registry.registerBeanDefinition(candidate.targetBeanName, candidate.definition)
        }

        val proxyName = "featureImplementation:${group.feature}"
        val definition = RootBeanDefinition(proxy.javaClass) {
            lock.write { if (group.current == null) group.current = select(group) }
            return@RootBeanDefinition proxy
        }
        definition.isPrimary = group.candidates.any { it.definition.isPrimary }
        registry.registerBeanDefinition(proxyName, definition)
        group.candidates.forEach { registry.registerAlias(proxyName, it.beanName) }
    }

    private fun select(group: ImplementationGroup): Any {
        val value = environment.getProperty("features.${group.feature}")
        val matching = group.candidates.filter { it.condition != null && it.condition == value }

        require(matching.size <= 1) {
            "Multiple implementations match feature '${group.feature}' with value '$value'"
        }
        val selected =
            requireNotNull(matching.singleOrNull() ?: group.candidates.singleOrNull { it.condition == null }) {
                "No implementation matches feature '${group.feature}' with value '$value' and no default is configured"
            }

        val target = beanFactory.getBean(selected.targetBeanName)
        beanFactory.registerDependentBean(selected.targetBeanName, "featureImplementation:${group.feature}")
        return target
    }

    private fun validateGroup(group: ImplementationGroup) {
        require(group.candidates.count { it.condition == null } <= 1) {
            "Multiple default implementations for feature '${group.feature}'"
        }

        val duplicates = group.candidates
            .filter { it.condition != null }
            .groupBy { it.condition }
            .filterValues { it.size > 1 }
            .keys

        require(duplicates.isEmpty()) {
            "Multiple implementations for feature '${group.feature}' with values $duplicates"
        }

        require(commonBusinessInterfaces(group).isNotEmpty()) {
            "Implementations for feature '${group.feature}' must share at least one business interface " +
                "that does not extend lifecycle or Aware contracts"
        }
    }

    private fun commonBusinessInterfaces(group: ImplementationGroup): Set<Class<*>> =
        group.candidates.map { candidate ->
            getAllInterfacesForClassAsSet(candidate.type)
                .flatMap { interfaceHierarchy(it) }
                .filterNot { type -> infrastructureInterfaces.any { it.isAssignableFrom(type) } }.toSet()
        }.reduce { common, next -> common.intersect(next) }

    private fun interfaceHierarchy(type: Class<*>): Set<Class<*>> = buildSet {
        add(type)
        type.interfaces.forEach { addAll(interfaceHierarchy(it)) }
    }
}

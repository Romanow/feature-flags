/*
 * Copyright (c) Romanov Alexey, 2025
 */
package ru.romanow.feature.flags.config

import org.springframework.aop.framework.AopProxyUtils
import org.springframework.beans.factory.ObjectFactory
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.config.Scope
import ru.romanow.feature.flags.annotations.ConditionOnFeatureEnabled
import ru.romanow.feature.flags.annotations.DefaultFeatureImplementation
import ru.romanow.feature.flags.properties.Features
import java.util.concurrent.ConcurrentHashMap

class FeatureScope(
    private val features: Features,
    private val beanFactory: ConfigurableListableBeanFactory
) : Scope {
    private val beans = ConcurrentHashMap<String, Any>()

    override fun get(name: String, objectFactory: ObjectFactory<*>): Any {
        val definition = beanFactory.getBeanDefinition(name)
        val beanClass = Class.forName(definition.beanClassName)
        var annotation = beanClass.getAnnotation(ConditionOnFeatureEnabled::class.java)
            ?: beanClass.getAnnotation(DefaultFeatureImplementation::class.java)

        val previousValue = featureState[feature]
        if (previousValue != null && previousValue != currentValue) {
            beans.remove(name)
        }

        return beans.computeIfAbsent(name) {
            featureState[feature] = currentValue
            objectFactory.`object`
        }
    }

    override fun remove(name: String): Any? {
        return beans.remove(name)
    }

    override fun registerDestructionCallback(name: String, callback: Runnable) {}
    override fun resolveContextualObject(key: String): Any? = null
    override fun getConversationId(): String = "feature"
}

/*
 * Copyright (c) Romanov Alexey, 2025
 */
package ru.romanow.feature.flags.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.ObjectFactory
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.config.Scope
import org.springframework.context.ApplicationContext
import org.springframework.core.env.Environment
import ru.romanow.feature.flags.annotations.ConditionOnFeatureEnabled
import ru.romanow.feature.flags.annotations.DefaultFeatureImplementation

class FeatureScope(
    private val beanFactory: ConfigurableListableBeanFactory,
    private val applicationContext: ApplicationContext,
    private val environment: Environment
) : Scope {
    private val logger = LoggerFactory.getLogger(FeatureScope::class.java)
    private val beans = mutableMapOf<String, Pair<String, String?>>()

    override fun get(name: String, objectFactory: ObjectFactory<*>): Any {
        val definition = beanFactory.getBeanDefinition(name)
        val beanClass = Class.forName(definition.beanClassName)
        val beanFeatureValue = beans[name]
        if (beanFeatureValue == null) {
            // Если beans пустой, значит это первый запрос и берем beanName, featureName, featureValue и кладем в map
            val featureName = featureName(beanClass)
            val featureValue = environment.getProperty("features.$featureName", String::class.java)
            beans[name] = Pair(featureName, featureValue)
        } else {
            // Если в beans уже содержится bean, то проверяем текущее значение environment[featureName] == featureValue,
            //  если оно не совпадает с тем, то надо обновить bean
            val featureName = featureName(beanClass)
            val previousFeatureValue = beans[name]?.second
            val currentFeatureValue = environment.getProperty("features.$featureName", String::class.java)
            if (previousFeatureValue != currentFeatureValue) {
                logger.info(
                    "Feature '$featureName' changed from '$previousFeatureValue' to " +
                        "'$currentFeatureValue', must change implementation"
                )
            }
        }
        return objectFactory.`object`
    }

    override fun remove(name: String): Any? {
        return beans.remove(name)
    }

    override fun registerDestructionCallback(name: String, callback: Runnable) {}
    override fun resolveContextualObject(key: String): Any? = null
    override fun getConversationId(): String = FEATURE_SCOPE

    private fun featureName(cls: Class<*>): String {
        if (cls.isAnnotationPresent(ConditionOnFeatureEnabled::class.java)) {
            return cls.getAnnotation(ConditionOnFeatureEnabled::class.java).feature
        } else if (cls.isAnnotationPresent(DefaultFeatureImplementation::class.java)) {
            return cls.getAnnotation(DefaultFeatureImplementation::class.java).feature
        }
        throw IllegalArgumentException(
            "Class $cls must be annotated with @ConditionOnFeatureEnabled or @DefaultFeatureImplementation"
        )
    }

    companion object {
        const val FEATURE_SCOPE = "feature"
    }
}

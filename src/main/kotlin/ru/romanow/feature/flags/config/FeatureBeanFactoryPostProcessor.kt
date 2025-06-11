/*
 * Copyright (c) Romanov Alexey, 2025
 */
package ru.romanow.feature.flags.config

import org.springframework.beans.factory.config.BeanFactoryPostProcessor
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.context.ApplicationContext
import org.springframework.core.env.Environment
import ru.romanow.feature.flags.config.FeatureScope.Companion.FEATURE_SCOPE

class FeatureBeanFactoryPostProcessor(
    private val environment: Environment,
    private val applicationContext: ApplicationContext
) : BeanFactoryPostProcessor {
    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
        val scope = FeatureScope(beanFactory, applicationContext, environment)
        (beanFactory as ConfigurableBeanFactory).registerScope(FEATURE_SCOPE, scope)
    }
}

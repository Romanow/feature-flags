/*
 * Copyright (c) Romanov Alexey, 2025
 */
package ru.romanow.feature.flags.config

import org.springframework.beans.factory.config.BeanFactoryPostProcessor
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import ru.romanow.feature.flags.properties.Features

class FeatureBeanFactoryPostProcessor(private val features: Features) : BeanFactoryPostProcessor {
    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
        val scope = FeatureScope(features, beanFactory)
        (beanFactory as ConfigurableBeanFactory).registerScope("feature", scope)
    }
}

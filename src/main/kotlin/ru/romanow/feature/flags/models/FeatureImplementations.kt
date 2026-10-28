/*
 * Copyright (c) Romanov Alexey, 2026
 */
package ru.romanow.feature.flags.models

import org.springframework.beans.factory.config.BeanDefinition

internal data class Candidate(
    val beanName: String,
    val definition: BeanDefinition,
    val feature: String,
    val condition: String?,
    val type: Class<*>
) {
    val targetBeanName = "featureTarget:$beanName"
}

internal class ImplementationGroup(val feature: String, val candidates: List<Candidate>) {
    var current: Any? = null
}

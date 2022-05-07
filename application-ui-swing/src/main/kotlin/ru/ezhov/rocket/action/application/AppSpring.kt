package ru.ezhov.rocket.action.application

import arrow.core.getOrHandle
import mu.KotlinLogging
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import ru.ezhov.rocket.action.application.config.AppConfigCoreSpring
import ru.ezhov.rocket.action.core.domain.repository.ActionRepository

private val logger = KotlinLogging.logger { }

fun main(args: Array<String>) {
    val appContext: ApplicationContext = AnnotationConfigApplicationContext(AppConfigCoreSpring::class.java)

    val actionRepository: ActionRepository = appContext.getBean(ActionRepository::class.java)

    val actions = actionRepository.all()

    println(actions.getOrHandle { throw IllegalArgumentException() }.size)
}

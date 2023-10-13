package ru.ezhov.rocket.action.application

import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import ru.ezhov.rocket.action.application.beanconfig.AppConfigCoreSpring

object ApplicationContextFactory {
    private var appContext = AnnotationConfigApplicationContext().apply {
        register(AppConfigCoreSpring::class.java)
        refresh()
    }

    fun context(): ApplicationContext = appContext
}

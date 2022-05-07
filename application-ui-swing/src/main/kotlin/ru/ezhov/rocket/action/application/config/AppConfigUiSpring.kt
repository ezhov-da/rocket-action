package ru.ezhov.rocket.action.application.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import ru.ezhov.rocket.action.application.BaseDialog

@Configuration
@ComponentScan(basePackages = ["ru.ezhov.rocket.action"])
open class AppConfigUiSpring {
    @Bean
    open fun baseDialog(): BaseDialog = BaseDialog
}

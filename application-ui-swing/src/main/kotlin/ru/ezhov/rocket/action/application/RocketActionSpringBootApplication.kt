package ru.ezhov.rocket.action.application

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration

/**
 * Spring Boot entry point of the application.
 *
 * The desktop UI is still created by [AppKt], but the Spring context and the
 * embedded servlet container are managed by Spring Boot.
 */
@SpringBootApplication(
    scanBasePackages = ["ru.ezhov.rocket.action"],
    // Quartz is used directly (not through spring-boot-starter-quartz), so its
    // auto-configuration would try to create a SchedulerFactoryBean backed by a
    // DataSource that the application does not have.
    exclude = [QuartzAutoConfiguration::class],
)
open class RocketActionSpringBootApplication

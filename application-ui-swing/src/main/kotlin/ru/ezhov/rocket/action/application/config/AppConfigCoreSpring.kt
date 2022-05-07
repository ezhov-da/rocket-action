package ru.ezhov.rocket.action.application.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import ru.ezhov.rocket.action.core.application.change.ChangeActionApplicationService
import ru.ezhov.rocket.action.core.application.change.ChangeActionApplicationServiceImpl
import ru.ezhov.rocket.action.core.application.change.UpdateActionSettingsApplicationService
import ru.ezhov.rocket.action.core.application.change.UpdateActionSettingsApplicationServiceImpl
import ru.ezhov.rocket.action.core.application.changeorder.ChangeOrderActionApplicationService
import ru.ezhov.rocket.action.core.application.changeorder.ChangeOrderActionApplicationServiceImpl
import ru.ezhov.rocket.action.core.application.create.CreateActionApplicationService
import ru.ezhov.rocket.action.core.application.create.CreateActionApplicationServiceImpl
import ru.ezhov.rocket.action.core.application.delete.DeleteActionApplicationService
import ru.ezhov.rocket.action.core.application.delete.DeleteActionApplicationServiceImpl
import ru.ezhov.rocket.action.core.application.get.GetActionApplicationService
import ru.ezhov.rocket.action.core.application.get.GetActionApplicationServiceImpl
import ru.ezhov.rocket.action.core.application.get.GetActionSettingsApplicationService
import ru.ezhov.rocket.action.core.application.get.GetActionSettingsApplicationServiceImpl
import ru.ezhov.rocket.action.core.domain.changeorder.AfterChangeOrderService
import ru.ezhov.rocket.action.core.domain.changeorder.AfterChangeOrderServiceImpl
import ru.ezhov.rocket.action.core.domain.changeorder.BeforeChangeOrderService
import ru.ezhov.rocket.action.core.domain.changeorder.BeforeChangeOrderServiceImpl
import ru.ezhov.rocket.action.core.domain.repository.ActionAndSettingsRepository
import ru.ezhov.rocket.action.core.domain.repository.ActionRepository
import ru.ezhov.rocket.action.core.domain.repository.ActionSettingsRepository
import ru.ezhov.rocket.action.core.infrastructure.db.DbCredentialsFactory
import ru.ezhov.rocket.action.core.infrastructure.db.KtormDbConnectionFactory
import ru.ezhov.rocket.action.core.infrastructure.db.h2.H2DbActionAndSettingsRepository
import ru.ezhov.rocket.action.core.infrastructure.db.h2.H2DbActionRepository
import ru.ezhov.rocket.action.core.infrastructure.db.h2.H2DbActionSettingsRepository
import ru.ezhov.rocket.action.core.infrastructure.db.h2.H2DbKtormDbConnectionFactory

@Configuration
@ComponentScan(basePackages = ["ru.ezhov.rocket.action"])
open class AppConfigCoreSpring {
    @Bean
    open fun ktormDbConnectionFactory(): KtormDbConnectionFactory =
        H2DbKtormDbConnectionFactory(credentials = object : DbCredentialsFactory {
            override val url: String
                get() = "jdbc:h2:./test-db"
            override val user: String
                get() = "rocket-action"
            override val password: String
                get() = "rocket-action"

        })

    @Bean
    open fun actionRepository(ktormDbConnectionFactory: KtormDbConnectionFactory): ActionRepository =
        H2DbActionRepository(factory = ktormDbConnectionFactory)

    @Bean
    open fun actionSettingsRepository(ktormDbConnectionFactory: KtormDbConnectionFactory): ActionSettingsRepository =
        H2DbActionSettingsRepository(factory = ktormDbConnectionFactory)

    @Bean
    open fun actionAndSettingsRepository(ktormDbConnectionFactory: KtormDbConnectionFactory): ActionAndSettingsRepository =
        H2DbActionAndSettingsRepository(factory = ktormDbConnectionFactory)

    @Bean
    open fun changeActionApplicationService(
        actionRepository: ActionRepository,
        actionSettingsRepository: ActionSettingsRepository,
    ): ChangeActionApplicationService =
        ChangeActionApplicationServiceImpl(
            actionRepository = actionRepository,
            actionSettingsRepository = actionSettingsRepository,
        )

    @Bean
    open fun updateActionSettingsApplicationService(
        actionSettingsRepository: ActionSettingsRepository
    ): UpdateActionSettingsApplicationService =
        UpdateActionSettingsApplicationServiceImpl(
            actionSettingsRepository = actionSettingsRepository
        )

    @Bean
    open fun afterChangeOrderService(actionRepository: ActionRepository): AfterChangeOrderService =
        AfterChangeOrderServiceImpl(actionRepository = actionRepository)

    @Bean
    open fun beforeChangeOrderService(actionRepository: ActionRepository): BeforeChangeOrderService =
        BeforeChangeOrderServiceImpl(actionRepository = actionRepository)

    @Bean
    open fun changeOrderActionApplicationService(
        afterChangeOrderService: AfterChangeOrderService,
        beforeChangeOrderService: BeforeChangeOrderService,
    ): ChangeOrderActionApplicationService =
        ChangeOrderActionApplicationServiceImpl(
            afterService = afterChangeOrderService,
            beforeService = beforeChangeOrderService,
        )

    @Bean
    open fun createActionApplicationService(
        actionAndSettingsRepository: ActionAndSettingsRepository
    ): CreateActionApplicationService =
        CreateActionApplicationServiceImpl(
            actionAndSettingsRepository = actionAndSettingsRepository
        )

    @Bean
    open fun deleteActionApplicationService(
        actionAndSettingsRepository: ActionAndSettingsRepository
    ): DeleteActionApplicationService =
        DeleteActionApplicationServiceImpl(
            actionAndSettingsRepository = actionAndSettingsRepository
        )

    @Bean
    open fun getActionApplicationService(actionRepository: ActionRepository): GetActionApplicationService =
        GetActionApplicationServiceImpl(
            actionRepository = actionRepository
        )

    @Bean
    open fun getActionSettingsApplicationService(
        actionSettingsRepository: ActionSettingsRepository
    ): GetActionSettingsApplicationService =
        GetActionSettingsApplicationServiceImpl(
            actionSettingsRepository = actionSettingsRepository
        )
}

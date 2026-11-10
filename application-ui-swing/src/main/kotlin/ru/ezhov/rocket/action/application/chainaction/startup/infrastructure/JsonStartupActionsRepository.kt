package ru.ezhov.rocket.action.application.chainaction.startup.infrastructure

import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import ru.ezhov.rocket.action.application.chainaction.startup.domain.StartupActionsRepository
import ru.ezhov.rocket.action.application.chainaction.startup.domain.model.StartupActions
import ru.ezhov.rocket.action.application.chainaction.startup.infrastructure.model.JsonStartupActions
import ru.ezhov.rocket.action.application.chainaction.startup.infrastructure.model.toJsonStartupActions
import ru.ezhov.rocket.action.application.chainaction.startup.infrastructure.model.toStartupActions
import ru.ezhov.rocket.action.application.properties.GeneralPropertiesRepository
import ru.ezhov.rocket.action.application.properties.UsedPropertiesName
import java.io.File

private val logger = KotlinLogging.logger {}

/**
 * Stores the startup list as a single ordered JSON file.
 *
 * A missing file means an empty list. A malformed file is logged and also means an empty list,
 * so a broken file can never prevent the application from starting.
 */
@Component
class JsonStartupActionsRepository(
    private val file: File,
    private val objectMapper: ObjectMapper,
) : StartupActionsRepository {
    private val lock = Any()

    @Autowired
    constructor(
        generalPropertiesRepository: GeneralPropertiesRepository,
        objectMapper: ObjectMapper,
    ) : this(
        file = File(
            generalPropertiesRepository.asStringOrNull(UsedPropertiesName.STARTUP_ACTIONS_FILE_REPOSITORY_PATH)
                ?: DEFAULT_FILE_PATH
        ),
        objectMapper = objectMapper,
    )

    override fun all(): StartupActions {
        synchronized(lock) {
            if (!file.exists()) {
                return StartupActions()
            }

            return try {
                objectMapper.readValue(file, JsonStartupActions::class.java).toStartupActions()
            } catch (ex: Exception) {
                logger.error(ex) {
                    "Error when read startup actions from file '${file.absolutePath}'. The empty list is used"
                }
                StartupActions()
            }
        }
    }

    override fun save(startupActions: StartupActions) {
        synchronized(lock) {
            file.parentFile?.mkdirs()
            objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValue(file, startupActions.toJsonStartupActions())
        }
    }

    companion object {
        const val DEFAULT_FILE_PATH = "./.rocket-action/startup-actions.json"
    }
}

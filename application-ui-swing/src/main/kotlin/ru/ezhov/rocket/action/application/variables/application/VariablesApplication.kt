package ru.ezhov.rocket.action.application.variables.application

import mu.KotlinLogging
import org.springframework.stereotype.Service
import ru.ezhov.rocket.action.application.applicationConfiguration.application.ConfigurationApplication
import ru.ezhov.rocket.action.application.applicationConfiguration.domain.model.ApplicationConfigurations
import ru.ezhov.rocket.action.application.applicationConfiguration.domain.model.VariablesManager
import ru.ezhov.rocket.action.application.core.application.ActionReloadScope
import ru.ezhov.rocket.action.application.encryption.application.EncryptionServiceFactory
import ru.ezhov.rocket.action.application.encryption.domain.EncryptionService
import ru.ezhov.rocket.action.application.encryption.domain.model.Algorithm
import ru.ezhov.rocket.action.application.properties.GeneralPropertiesRepository
import ru.ezhov.rocket.action.application.properties.UsedPropertiesName
import ru.ezhov.rocket.action.application.variables.domain.VariableRepository
import ru.ezhov.rocket.action.application.variables.domain.model.Encryption
import ru.ezhov.rocket.action.application.variables.domain.model.Variable
import ru.ezhov.rocket.action.application.variables.domain.model.VariableType
import ru.ezhov.rocket.action.application.variables.domain.model.Variables
import ru.ezhov.rocket.action.application.variables.infrastructure.KeePassVariableRepository
import java.io.File
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

private val logger = KotlinLogging.logger { }

private const val DEFAULT_VARIABLES_FILE = "./.rocket-action/variables.json"
private const val DEFAULT_CONFIGURATION_FILE = "./.rocket-action/configurations.json"
private val KEEPASS_BACKOFF_NANOS = TimeUnit.SECONDS.toNanos(30)

/**
 * User variables and the external password store.
 *
 * A reload needs the same variables for hundreds of settings, so the resolved snapshot is memoised.
 * The memo is keyed by a fingerprint of the source files (path, size, modification time) and the
 * configuration values, which also covers edits made outside the application. Concurrent callers
 * share one computation, so the password store is opened and decrypted once.
 */
@Service
class VariablesApplication(
    private val variableRepository: VariableRepository,
    private val configurationApplication: ConfigurationApplication,
    private val generalPropertiesRepository: GeneralPropertiesRepository,
) {
    private val defaultAlgorithm = Algorithm.BLOWFISH
    private val encryptionService = EncryptionServiceFactory.get(defaultAlgorithm)!!

    private val variablesFilePath = generalPropertiesRepository
        .asStringOrNull(UsedPropertiesName.VARIABLES_FILE_REPOSITORY_PATH)
        ?: DEFAULT_VARIABLES_FILE

    private val configurationFilePath = generalPropertiesRepository
        .asStringOrNull(UsedPropertiesName.APPLICATION_CONFIGURATION_FILE_REPOSITORY_PATH)
        ?: DEFAULT_CONFIGURATION_FILE

    private val lock = Any()

    @Volatile
    private var snapshot: Snapshot? = null

    @Volatile
    private var inFlight: CompletableFuture<VariablesDto>? = null

    private var generation = 0L

    @Volatile
    private var keePassFailedUntilNanos = 0L

    fun all(): VariablesDto {
        val configuration = configurationApplication.all()
        val fingerprint = fingerprint(configuration)

        snapshot?.takeIf { it.fingerprint == fingerprint }?.let {
            ActionReloadScope.recordVariablesLoad(0)
            return it.variables
        }

        val future: CompletableFuture<VariablesDto>
        val owner: Boolean
        synchronized(lock) {
            val current = snapshot
            if (current != null && current.fingerprint == fingerprint) {
                ActionReloadScope.recordVariablesLoad(0)
                return current.variables
            }

            val existing = inFlight
            if (existing != null) {
                future = existing
                owner = false
            } else {
                future = CompletableFuture()
                inFlight = future
                owner = true
            }
        }

        if (!owner) {
            val joined = future.join()
            val current = snapshot
            return if (current != null && current.fingerprint == fingerprint) current.variables else joined
        }

        val generationAtStart = synchronized(lock) { generation }
        try {
            val startedAt = System.nanoTime()
            val computed = compute(configuration)
            ActionReloadScope.recordVariablesLoad(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt))
            synchronized(lock) {
                if (generation == generationAtStart) {
                    snapshot = Snapshot(fingerprint = fingerprint, variables = computed)
                }
            }
            future.complete(computed)
            return computed
        } catch (throwable: Throwable) {
            future.completeExceptionally(throwable)
            throw throwable
        } finally {
            synchronized(lock) {
                if (inFlight === future) {
                    inFlight = null
                }
            }
        }
    }

    /**
     * Forces the next [all] to re-read the sources and resets the password-store back-off.
     */
    fun invalidate() {
        synchronized(lock) {
            generation++
            snapshot = null
            inFlight = null
            keePassFailedUntilNanos = 0L
        }
    }

    private fun compute(configuration: ApplicationConfigurations): VariablesDto {
        val key = configuration.variablesKey
        val originVariables = variableRepository.all()
        val variables = if (originVariables.encryption != null) {
            val algorithm = Algorithm.of(originVariables.encryption.algorithm)
            if (algorithm != null) {
                val service = EncryptionServiceFactory.get(algorithm)
                if (service != null) {
                    decode(originVariables, key, service)
                } else {
                    logger.warn { "Encryption service not found by algorithm '$algorithm'" }
                    originVariables.toVariablesDto(key)
                }
            } else {
                logger.warn { "Algorithm not found by algorithm '${originVariables.encryption.algorithm}'" }
                originVariables.toVariablesDto(key)
            }
        } else {
            logger.warn { "User variables is not encoded! Please save variables with encode" }
            originVariables.toVariablesDto(key)
        }

        // For KeePass you need to get all the variables and get the password
        val keePassVariables = readKeePassVariables(variables, configuration)

        return VariablesDto(variables.key, variables = variables.variables + keePassVariables.map { it.toDto() })
    }

    private fun readKeePassVariables(
        variables: VariablesDto,
        configuration: ApplicationConfigurations,
    ): List<Variable> {
        if (System.nanoTime() < keePassFailedUntilNanos) {
            return emptyList()
        }

        return try {
            KeePassVariableRepository(
                variables.variables,
                configuration.variablesManagers
            ).all()
        } catch (ex: Exception) {
            // A broken password store must not break the reload: log once and skip its entries
            // for a short back-off so every setting does not retry the read.
            keePassFailedUntilNanos = System.nanoTime() + KEEPASS_BACKOFF_NANOS
            logger.warn(ex) {
                "Error when read the password store. KeePass variables are skipped for 30 seconds"
            }
            emptyList()
        }
    }

    private fun fingerprint(configuration: ApplicationConfigurations): String = buildString {
        appendFile(File(variablesFilePath))
        appendFile(File(configurationFilePath))
        configuration.variablesManagers
            .filterIsInstance<VariablesManager.KeePassVariablesManager>()
            .forEach { appendFile(File(it.dbPath)) }

        append("|key=").append(configuration.variablesKey)
        configuration.variablesManagers.forEach { append("|manager=").append(it.toString()) }
    }

    private fun StringBuilder.appendFile(file: File) {
        append("|").append(file.absolutePath)
            .append(":").append(file.length())
            .append(":").append(file.lastModified())
    }

    private fun decode(variables: Variables, key: String, encryptionService: EncryptionService): VariablesDto =
        VariablesDto(
            key = key,
            variables = variables.variables.map { v ->
                val value = if (v.type == VariableType.APPLICATION) {
                    encryptionService.decrypt(v.value, key)
                } else {
                    v.value
                }

                VariableDto(
                    name = v.name,
                    value = value,
                    description = v.description,
                    type = v.type,
                )

            }
        )

    private fun encode(variables: VariablesDto, encryptionService: EncryptionService): Variables =
        Variables(
            encryption = Encryption(algorithm = defaultAlgorithm.name),
            variables = variables.variables.map { v ->
                Variable(
                    name = v.name,
                    value = encryptionService.encrypt(source = v.value, key = variables.key),
                    description = v.description,
                    type = v.type,
                )

            }
        )

    fun save(variables: VariablesDto) {
        val applicationConfig = configurationApplication.all()
        applicationConfig.variablesKey = variables.key
        val resultVariables = encode(variables = variables, encryptionService = encryptionService)

        variableRepository.save(resultVariables)
        configurationApplication.save(applicationConfig)

        invalidate()
    }

    fun updateVariable(key: String, value: String) {
        val configuration = configurationApplication.all()
        val all = all().variables.filter { it.type == VariableType.APPLICATION }.toMutableList()
        val variableForUpdate = all()
            .variables
            .firstOrNull { it.name == key && it.type == VariableType.APPLICATION }
            ?: throw RuntimeException("Not found application variable '$key'")

        val withNewValue = variableForUpdate.updateValue(value)
        all.remove(variableForUpdate)
        all.add(withNewValue)

        val resultVariables = encode(
            variables = VariablesDto(
                key = configuration.variablesKey,
                variables = all
            ), encryptionService = encryptionService
        )

        variableRepository.save(resultVariables)

        invalidate()
    }

    private data class Snapshot(
        val fingerprint: String,
        val variables: VariablesDto,
    )
}

private fun Variables.toVariablesDto(key: String): VariablesDto =
    VariablesDto(
        key = key,
        variables = this.variables.map { it.toDto() }
    )

private fun Variable.toDto() = VariableDto(
    name = this.name,
    value = this.value,
    description = this.description,
    type = this.type,
)

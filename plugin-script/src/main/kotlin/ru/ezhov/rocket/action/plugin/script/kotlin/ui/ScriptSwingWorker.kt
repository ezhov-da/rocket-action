package ru.ezhov.rocket.action.plugin.script.kotlin.ui

import arrow.core.Either
import arrow.core.getOrHandle
import mu.KotlinLogging
import ru.ezhov.rocket.action.notification.NotificationFactory
import ru.ezhov.rocket.action.notification.NotificationType
import ru.ezhov.rocket.action.plugin.script.kotlin.application.KotlinScriptEngine
import ru.ezhov.rocket.action.plugin.script.kotlin.application.ScriptEngineException
import javax.swing.SwingWorker

private val logger = KotlinLogging.logger {}

class ScriptSwingWorker(
    beforeExecuteCallback: () -> Unit,
    private val afterExecuteCallback: (String) -> Unit,
    private val script: String,
) : SwingWorker<Either<ScriptEngineException, Any?>, Any>() {
    private val scriptEngine = KotlinScriptEngine()

    init {
        beforeExecuteCallback()
    }

    override fun doInBackground(): Either<ScriptEngineException, Any?> =
        scriptEngine.execute(script)

    override fun done() {
        val textAsObject = this.get().getOrHandle {
            logger.warn(it) { "Error script executed. Script $script" }
            NotificationFactory.notification.show(
                type = NotificationType.WARN,
                text = "Ошибка выполнения скрипта"
            )
            it.message ?: "Error"
        }
        val text = textAsObject?.toString() ?: "null result"
        afterExecuteCallback(text)
    }
}

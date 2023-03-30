package ru.ezhov.rocket.action.plugin.jira.worklog.ui

import arrow.core.Either
import arrow.core.right
import ru.ezhov.rocket.action.api.context.RocketActionContext
import ru.ezhov.rocket.action.api.context.cache.CacheService
import ru.ezhov.rocket.action.api.context.icon.IconService
import ru.ezhov.rocket.action.api.context.notification.NotificationService
import ru.ezhov.rocket.action.api.context.search.Search
import ru.ezhov.rocket.action.plugin.jira.worklog.domain.CommitTimeService
import ru.ezhov.rocket.action.plugin.jira.worklog.domain.CommitTimeServiceException
import ru.ezhov.rocket.action.plugin.jira.worklog.domain.CommitTimeTaskInfoException
import ru.ezhov.rocket.action.plugin.jira.worklog.domain.CommitTimeTaskInfoRepository
import ru.ezhov.rocket.action.plugin.jira.worklog.domain.model.AliasForTaskIds
import ru.ezhov.rocket.action.plugin.jira.worklog.domain.model.CommitTimeTask
import ru.ezhov.rocket.action.plugin.jira.worklog.domain.model.CommitTimeTaskInfo
import ru.ezhov.rocket.action.plugin.jira.worklog.domain.model.Task
import ru.ezhov.rocket.action.plugin.jira.worklog.domain.validations.Validator
import java.net.URI
import java.nio.file.Files
import javax.swing.JFrame
import javax.swing.SwingUtilities
import javax.swing.UIManager
import kotlin.random.Random

fun main() {
    SwingUtilities.invokeLater {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        } catch (ex: Throwable) {
            //
        }
        val frame = JFrame("_________")
        frame.add(
            CommitTimePanel(
                tasks = listOf(
                    Task(
                        id = "123",
                        name = "ляляля",
                    ),
                    Task(
                        id = "234",
                        name = "432",
                    ),
                ),
                commitTimeService = object : CommitTimeService {
                    override fun commit(task: CommitTimeTask): Either<CommitTimeServiceException, Unit> {
                        val random = Random.nextLong(2000)
                        Thread.sleep(random)
                        println("commit task after: $random ms")
                        return Unit.right()
                    }
                },
                delimiter = "_",
                dateFormatPattern = "yyyyMMddHH",
                constantsNowDate = listOf("n", "т"),
                aliasForTaskIds = AliasForTaskIds.of(
                    """
                      123_тру,ля,пв,в
                      122_тру,ля
                    """.trimIndent()
                ),
                linkToWorkLog = URI.create("https://google.com"),
                fileForSave = Files.createTempFile("test", "test").toFile(),
                commitTimeTaskInfoRepository = object : CommitTimeTaskInfoRepository {
                    override fun info(id: String): Either<CommitTimeTaskInfoException, CommitTimeTaskInfo?> =
                        CommitTimeTaskInfo(name = "$id + типа имя").right()
                },
                context = object : RocketActionContext {
                    override fun icon(): IconService {
                        TODO("Not yet implemented")
                    }

                    override fun notification(): NotificationService {
                        TODO("Not yet implemented")
                    }

                    override fun cache(): CacheService {
                        TODO("Not yet implemented")
                    }

                    override fun search(): Search {
                        TODO("Not yet implemented")
                    }
                },
                validator = object : Validator {
                    override fun validate(source: String): List<String> = emptyList()
                }
            )
        )

        frame.setSize(1000, 600)
        frame.setLocationRelativeTo(null)
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.isVisible = true
    }
}

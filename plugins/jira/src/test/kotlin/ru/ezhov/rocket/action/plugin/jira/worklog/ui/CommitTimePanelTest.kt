package ru.ezhov.rocket.action.plugin.jira.worklog.ui

import arrow.core.Either
import arrow.core.right
import io.mockk.mockk
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
                        id = "111",
                        name = "ляляля",
                    ),
                    Task(
                        id = "222",
                        name = "432",
                    ),
                    Task(
                        id = "333",
                        name = "432",
                    ),
                    Task(
                        id = "444",
                        name = "432",
                    ),
                    Task(
                        id = "555",
                        name = "432",
                    ),
                    Task(
                        id = "666",
                        name = "432",
                    ),
                    Task(
                        id = "777",
                        name = "432",
                    ),
                    Task(
                        id = "888",
                        name = "432",
                    ),
                    Task(
                        id = "999",
                        name = "432",
                    ),
                    Task(
                        id = "123",
                        name = "432",
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
                      111_тру,ля,пв,в
                      222_аля,го
                    """.trimIndent()
                ),
                linkToWorkLog = URI.create("https://google.com"),
                fileForSave = Files.createTempFile("test", "test").toFile(),
                commitTimeTaskInfoRepository = object : CommitTimeTaskInfoRepository {
                    override fun info(id: String): Either<CommitTimeTaskInfoException, CommitTimeTaskInfo?> =
                        CommitTimeTaskInfo(name = "$id + type name").right()
                },
                context = object : RocketActionContext {
                    override fun icon(): IconService = mockk()

                    override fun notification(): NotificationService = mockk()

                    override fun cache(): CacheService = mockk()

                    override fun search(): Search = mockk()
                },
                validator = object : Validator {
                    override fun validate(source: String): List<String> = emptyList()
                },
                maxTimeInMinutes = 480,
            )
        )

        frame.setSize(1000, 600)
        frame.setLocationRelativeTo(null)
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.isVisible = true
    }
}

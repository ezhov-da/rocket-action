package ru.ezhov.rocket.action.application.chainaction.interfaces.ui.base

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import ru.ezhov.rocket.action.application.applicationConfiguration.application.ConfigurationApplication
import ru.ezhov.rocket.action.application.applicationConfiguration.domain.model.ApplicationConfigurations
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicActionEngine
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicActionSource
import ru.ezhov.rocket.action.application.chainaction.domain.model.ChainAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.ContractType
import ru.ezhov.rocket.action.application.chainaction.scheduler.application.ActionSchedulerService
import ru.ezhov.rocket.action.application.search.application.SearchTextTransformer
import java.awt.Component
import java.awt.Container
import java.util.concurrent.TimeUnit
import javax.swing.JToggleButton

class SelectChainPopupMenuTest {
    private val configuration = ApplicationConfigurations(
        variablesKey = "",
        variablesManagers = emptyList(),
        numberButtonsOnChainActionSelectionPanel = 5,
        globalHotKeys = null,
    )
    private val configurationApplication = mockk<ConfigurationApplication>(relaxed = true) {
        every { all() } returns configuration
    }
    private val actionSchedulerService = mockk<ActionSchedulerService>(relaxed = true) {
        every { schedulers() } returns emptyList()
    }

    private val chains = (0 until 12).map { chain("chain-%02d".format(it), "Chain %02d".format(it)) }
    private val atomics = (0 until 12).map { atomic("atomic-%02d".format(it), "Atomic %02d".format(it)) }

    @Test
    @Timeout(10)
    fun `changing the button count keeps every action available in the list`() {
        val first = createPopup()

        waitUntil { first.listPanel()!!.currentItems().map { it.id() }.toSet() == remainder(5) }
        assertThat(first.listPanel()!!.currentItems().map { it.id() }.toSet()).isEqualTo(remainder(5))

        val tenButtons = toggle(first, "10")
        tenButtons.doClick()

        waitUntil { first.listPanel()!!.currentItems().map { it.id() }.toSet() == remainder(10) }
        assertThat(first.listPanel()!!.currentItems().map { it.id() }.toSet()).isEqualTo(remainder(10))
    }

    private fun createPopup() = SelectChainPopupMenu(
        configurationApplication = configurationApplication,
        searchTextTransformer = SearchTextTransformer(),
        actionSchedulerService = actionSchedulerService,
        chains = chains,
        atomics = atomics,
        selectedChainCallback = {},
    )

    private fun remainder(partCount: Int): Set<String> =
        (chains.map { it.id() }.drop(partCount) + atomics.map { it.id() }.drop(partCount)).toSet()

    private fun toggle(component: Component, text: String): JToggleButton {
        if (component is JToggleButton && component.text == text) {
            return component
        }
        if (component is Container) {
            component.components.forEach { child ->
                val found = runCatching { toggle(child, text) }.getOrNull()
                if (found != null) {
                    return found
                }
            }
        }
        throw AssertionError("Toggle button '$text' was not found")
    }

    private fun waitUntil(condition: () -> Boolean) {
        val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5)
        while (System.nanoTime() < deadline) {
            if (condition()) {
                return
            }
            Thread.sleep(10)
        }
        throw AssertionError("Condition was not met in time")
    }

    private fun chain(id: String, name: String) = ChainAction(
        id = id,
        name = name,
        description = name,
        actions = emptyList(),
    )

    private fun atomic(id: String, name: String) = AtomicAction(
        id = id,
        name = name,
        description = name,
        contractType = ContractType.IN_OUT,
        engine = AtomicActionEngine.GROOVY,
        source = AtomicActionSource.TEXT,
        data = name,
    )
}

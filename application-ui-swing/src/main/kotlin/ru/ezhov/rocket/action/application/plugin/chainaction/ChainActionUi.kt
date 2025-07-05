package ru.ezhov.rocket.action.application.plugin.chainaction

import mu.KotlinLogging
import net.miginfocom.swing.MigLayout
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import org.fife.ui.rtextarea.RTextScrollPane
import ru.ezhov.rocket.action.api.PropertyComponent
import ru.ezhov.rocket.action.api.RocketAction
import ru.ezhov.rocket.action.api.RocketActionConfiguration
import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.api.RocketActionFactoryUi
import ru.ezhov.rocket.action.api.RocketActionPlugin
import ru.ezhov.rocket.action.api.RocketActionPluginInfo
import ru.ezhov.rocket.action.api.RocketActionPropertySpec
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.api.RocketActionType
import ru.ezhov.rocket.action.api.context.RocketActionContext
import ru.ezhov.rocket.action.api.support.AbstractRocketAction
import ru.ezhov.rocket.action.application.chainaction.application.AtomicActionService
import ru.ezhov.rocket.action.application.chainaction.application.ChainActionService
import ru.ezhov.rocket.action.application.chainaction.domain.ActionExecutor
import ru.ezhov.rocket.action.application.chainaction.domain.ProgressExecutingAction
import ru.ezhov.rocket.action.application.chainaction.domain.event.AtomicActionUpdatedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.domain.event.ChainActionUpdatedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.domain.model.Action
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import ru.ezhov.rocket.action.application.chainaction.infrastructure.ActionExecutorFactory
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.base.SelectChainListPanel
import ru.ezhov.rocket.action.application.chainaction.scheduler.application.ActionSchedulerService
import ru.ezhov.rocket.action.application.event.domain.DomainEvent
import ru.ezhov.rocket.action.application.event.domain.DomainEventSubscriber
import ru.ezhov.rocket.action.application.event.infrastructure.DomainEventFactory
import ru.ezhov.rocket.action.application.resources.Icons
import ru.ezhov.rocket.action.application.search.application.SearchTextTransformer
import ru.ezhov.rocket.action.plugin.clipboard.ClipboardUtil
import ru.ezhov.rocket.action.ui.utils.swing.common.SizeUtil
import ru.ezhov.rocket.action.ui.utils.swing.common.TextFieldWithText
import ru.ezhov.rocket.action.ui.utils.swing.common.toIcon
import java.awt.BorderLayout
import java.awt.Component
import java.util.*
import javax.swing.Icon
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JMenu
import javax.swing.JMenuItem
import javax.swing.JPanel
import javax.swing.SwingWorker

private val logger = KotlinLogging.logger { }

class ChainActionUi : AbstractRocketAction(), RocketActionPlugin {
    private var chainActionService: ChainActionService? = null
    private var atomicActionService: AtomicActionService? = null
    private var actionExecutor: ActionExecutor? = null
    private var searchTextTransformer: SearchTextTransformer? = null
    private var actionSchedulerService: ActionSchedulerService? = null

    private var actionContext: RocketActionContext? = null

    override fun factory(context: RocketActionContext): RocketActionFactoryUi = this
        .apply {
            actionContext = context
        }

    override fun configuration(context: RocketActionContext): RocketActionConfiguration = this
        .apply {
            actionContext = context
            chainActionService = ChainActionService.INSTANCE
            atomicActionService = AtomicActionService.INSTANCE
            actionExecutor = ActionExecutorFactory.INSTANCE
            searchTextTransformer = SearchTextTransformer.INSTANCE
            actionSchedulerService = ActionSchedulerService.INSTANCE
        }

    override fun info(): RocketActionPluginInfo = object : RocketActionPluginInfo {
        override fun version(): String = Properties().let {
            it.load(this.javaClass.getResourceAsStream("/general.properties"))
            it.getProperty("rocket.action.version")
        }

        override fun author(): String = "DEzhov"

        override fun link(): String? = null
    }

    override fun create(settings: RocketActionSettings, context: RocketActionContext): RocketAction? =
        settings.settings()[ID]?.takeIf { it.isNotEmpty() }?.let { id ->
            (chainActionService!!.byId(id) ?: atomicActionService!!.atomicBy(id))?.let { action ->
                settings.settings()[UI_TYPE]?.let { uiType ->
                    createByType(action, uiType)?.let { component ->
                        object : RocketAction {
                            override fun contains(search: String): Boolean = false

                            override fun isChanged(actionSettings: RocketActionSettings): Boolean =
                                (settings.id() != actionSettings.id())

                            override fun component(): Component = component
                        }
                    }
                }
            }
        }

    override fun type(): RocketActionType = RocketActionType { TYPE }

    override fun description(): String = "Allows you to run a chain and atomic actions"

    override fun asStringDynamic(settings: Map<String, String>): String? =
        settings[ID]
            ?.let {
                (chainActionService!!.byId(it) ?: atomicActionService!!.atomicBy(it))
                    ?.name()
            }

    override fun asString(): List<String> = listOf(ID)

    override fun properties(): List<RocketActionConfigurationProperty> {
        return listOf(
            createRocketActionProperty(
                key = ID,
                name = "Action ID",
                description = "Action ID",
                required = true,
                property = RocketActionPropertySpec.ComponentPropertySpec(
                    ActionsComponentPanel(
                        chainActionService = chainActionService!!,
                        atomicActionService = atomicActionService!!,
                        searchTextTransformer = searchTextTransformer!!,
                        actionSchedulerService = actionSchedulerService!!,
                    )
                )
            ),
            createRocketActionProperty(
                key = UI_TYPE,
                name = "UI type",
                description = "UI type",
                required = true,
                property = RocketActionPropertySpec.ListPropertySpec(
                    defaultValue = TYPE_BUTTON,
                    valuesForSelect = listOf(
                        TYPE_BUTTON,
                        TYPE_MENU_WITH_INPUT,
                        TYPE_MENU_WITHOUT_INPUT_AND_BUTTON,
                        TYPE_MENU_WITH_INPUT_AND_BUTTON,
                    )
                )
            ),
        )
    }

    private fun createByType(action: Action, type: String): Component? {
        val updateActionListeners = mutableListOf<(action: Action) -> Unit>()

        DomainEventFactory.subscriberRegistrar.subscribe(object : DomainEventSubscriber {
            override fun handleEvent(event: DomainEvent) {
                when (event) {
                    is ChainActionUpdatedDomainEvent -> event.chainAction
                    is AtomicActionUpdatedDomainEvent -> event.atomicAction
                    else -> null
                }?.let { ac ->
                    if (ac.id() == action.id()) {
                        updateActionListeners.forEach { it(ac) }
                    }
                }
            }

            override fun subscribedToEventType(): List<Class<*>> = listOf(
                ChainActionUpdatedDomainEvent::class.java,
                AtomicActionUpdatedDomainEvent::class.java,
            )
        })

        val component = when (type) {
            TYPE_BUTTON -> {
                JMenuItem(action.name()).apply {
                    updateActionListeners.add { ac ->
                        name = ac.name()
                        toolTipText = ac.description()
                        icon = ac.calculateIcon()
                    }
                    toolTipText = action.description()
                    icon = action.calculateIcon()
                    addActionListener {
                        RunWorker(
                            actionExecutor = actionExecutor!!,
                            action = action,
                            input = null,
                            callback = null,
                        ).execute()
                    }
                }
            }

            TYPE_MENU_WITH_INPUT -> {
                JMenu(action.name()).apply {
                    icon = action.calculateIcon()

                    val textField = TextFieldWithText("Text")
                    textField.columns = 10
                    textField.toolTipText = action.description()

                    updateActionListeners.add { ac ->
                        text = ac.name()
                        textField.toolTipText = ac.description()
                        icon = ac.calculateIcon()
                    }

                    val actionOnTextField: (text: String) -> Unit = { text ->
                        RunWorker(
                            actionExecutor = actionExecutor!!,
                            action = action,
                            input = text,
                            callback = null,
                        ).execute()
                    }
                    textField.addActionListener {
                        textField
                            .text
                            ?.takeIf { it.isNotEmpty() }
                            ?.let { t -> actionOnTextField(t) }
                    }
                    add(textField)
                }
            }

            TYPE_MENU_WITHOUT_INPUT_AND_BUTTON -> {
                JMenu(action.name()).apply {

                    updateActionListeners.add { ac ->
                        text = ac.name()
                        toolTipText = ac.description()
                        icon = ac.calculateIcon()
                    }

                    icon = action.calculateIcon()
                    toolTipText = action.description()
                    add(RunPanel(actionExecutor = actionExecutor!!, action = action, showInputField = false))
                }
            }

            TYPE_MENU_WITH_INPUT_AND_BUTTON -> {
                JMenu(action.name()).apply {

                    updateActionListeners.add { ac ->
                        text = ac.name()
                        toolTipText = ac.description()
                        icon = ac.calculateIcon()
                    }

                    icon = action.calculateIcon()
                    toolTipText = action.description()
                    add(RunPanel(actionExecutor = actionExecutor!!, action = action, showInputField = true))
                }
            }

            else -> null
        }

        return component
    }

    private fun Action.calculateIcon(): ImageIcon = this.icon()?.toIcon() ?: Icons.Advanced.ROCKET_BLACK_16x16

    override fun name(): String = "Run chain or atomic action"

    override fun icon(): Icon = Icons.Advanced.ROCKET_BLACK_16x16

    companion object {
        const val TYPE = "CHAIN_OR_ATOMIC_ACTION"
        private const val ID = "actionId"
        private const val UI_TYPE = "uiType"

        private const val TYPE_BUTTON = "BUTTON"
        private const val TYPE_MENU_WITH_INPUT = "MENU_WITH_INPUT"
        private const val TYPE_MENU_WITHOUT_INPUT_AND_BUTTON = "MENU_WITHOUT_INPUT_AND_RESULT"
        private const val TYPE_MENU_WITH_INPUT_AND_BUTTON = "MENU_WITH_INPUT_AND_RESULT"
    }
}

private class ActionsComponentPanel(
    chainActionService: ChainActionService,
    atomicActionService: AtomicActionService,
    searchTextTransformer: SearchTextTransformer,
    actionSchedulerService: ActionSchedulerService,
) : JPanel(BorderLayout()), PropertyComponent {
    private var selectedPanel: SelectChainListPanel

    init {
        selectedPanel = SelectChainListPanel(
            actionService = atomicActionService,
            chainActionService = chainActionService,
            chains = chainActionService.chains(),
            atomics = atomicActionService.atomics(),
            searchTextTransformer = searchTextTransformer,
            actionSchedulerService = actionSchedulerService,
            selectedChainCallback = { },
        )

        add(selectedPanel, BorderLayout.CENTER)
    }

    override fun component(): Component = this

    override fun getPropertyValue(): String? = selectedPanel.selectedAction()?.id()

    override fun setPropertyValueValue(value: String) {
        selectedPanel.setSelectedAction(value)
    }
}

private class RunPanel(
    actionExecutor: ActionExecutor,
    action: Action,
    showInputField: Boolean = true,
) : JPanel(BorderLayout()) {
    private val textField = TextFieldWithText("Input")
    private val resultField = RSyntaxTextArea()
    private val runButton = JButton("Execute")
    private val copyToClipboard = JButton("Copy to clipboard")


    init {
        if (showInputField) {
            add(textField, BorderLayout.NORTH)
        }
        add(RTextScrollPane(resultField), BorderLayout.CENTER)

        add(
            JPanel(MigLayout()).apply {
                add(runButton)
                add(copyToClipboard.apply {
                    copyToClipboard.addActionListener {
                        ClipboardUtil.copyToClipboard(resultField.text)
                    }
                })

            },
            BorderLayout.SOUTH
        )


        runButton.addActionListener {
            RunWorker(
                actionExecutor = actionExecutor,
                action = action,
                input = textField.text,
                callback = { text ->
                    resultField.text = text
                    if (text.isNotEmpty()) {
                        resultField.caretPosition = 0
                    }
                },
            ).execute()
        }

        SizeUtil.dimension(0.3, 0.2).let {
            SizeUtil.setAllSize(this, it)
        }
    }
}

private class RunWorker(
    private val actionExecutor: ActionExecutor,
    private val action: Action,
    private val input: String?,
    private val callback: ((String) -> Unit)?,
) : SwingWorker<String?, String>() {
    override fun doInBackground(): String? {
        var executeResult: String? = null

        actionExecutor.execute(
            input = input,
            action = action,
            progressExecutingAction = object : ProgressExecutingAction {
                override fun onComplete(result: Any?, lastAtomicAction: AtomicAction) {
                    executeResult = result?.toString()
                }

                override fun onAtomicActionSuccess(orderId: String, result: Any?, atomicAction: AtomicAction) {
                    // not interested
                }

                override fun onAtomicActionFailure(orderId: String, atomicAction: AtomicAction?, ex: Exception) {
                    throw Exception("Error when execute action by ID '${atomicAction?.id}'", ex)
                }
            }
        )

        return executeResult
    }

    override fun done() {
        callback?.let { cb ->
            try {
                get()?.let {
                    cb.invoke(it)
                }
            } catch (ex: Exception) {
                cb.invoke("${ex.message}\n${ex.stackTraceToString()}")
            }
        }
    }
}

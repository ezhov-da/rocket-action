package ru.ezhov.rocket.action.application.chainaction.interfaces.ui

import ru.ezhov.rocket.action.application.chainaction.application.AtomicActionService
import ru.ezhov.rocket.action.application.chainaction.domain.model.ChainAction
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.components.iconForContractTypes
import javax.swing.Icon
import javax.swing.ImageIcon

object UiUtils

val CHAIN_ICON = ImageIcon(UiUtils::class.java.getResource("/icons/chain_16x16.png"))

val ATOMIC_ICON = ImageIcon(UiUtils::class.java.getResource("/icons/gear_16x16.png"))

/**
 * Temporary solution, should be adjusted to the type of contract in [ChainAction]
 */
fun chainIcon(chain: ChainAction, atomicActionService: AtomicActionService): Icon? {
    val firstAtomicAction = chain.actions.firstOrNull()?.let { actionOrder ->
        atomicActionService.atomicBy(actionOrder.actionId)
    }
    val lastAtomicAction = chain.actions.lastOrNull()?.let { actionOrder ->
        atomicActionService.atomicBy(actionOrder.actionId)
    }

    return iconForContractTypes(
        first = firstAtomicAction?.contractType,
        second = lastAtomicAction?.contractType
    )
}

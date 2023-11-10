package ru.ezhov.rocket.action.application.chainaction.interfaces.ui.components

import ru.ezhov.rocket.action.application.chainaction.domain.model.ContractType
import ru.ezhov.rocket.action.application.chainaction.domain.model.InputValueContractType
import ru.ezhov.rocket.action.application.chainaction.domain.model.OutputValueContractType
import ru.ezhov.rocket.action.application.resources.Icons
import tips4java.CompoundIcon
import javax.swing.Icon

object ChainIcons {
    val IN_IMAGE_ICON = Icons.Standard.Small.CHEVRON_RIGHT
    val OUT_IMAGE_ICON = Icons.Standard.Small.CHEVRON_LEFT
    val UNIT_IMAGE_ICON = Icons.Standard.Small.MEDIA_RECORD

    val IN_OUT_ICON = CompoundIcon(IN_IMAGE_ICON, OUT_IMAGE_ICON)
    val IN_UNIT_ICON = CompoundIcon(IN_IMAGE_ICON, UNIT_IMAGE_ICON)
    val UNIT_OUT_ICON = CompoundIcon(UNIT_IMAGE_ICON, OUT_IMAGE_ICON)
    val UNIT_UNIT_ICON = CompoundIcon(UNIT_IMAGE_ICON, UNIT_IMAGE_ICON)
}

fun ContractType.toIcon(): Icon = when (this) {
    ContractType.IN_OUT -> ChainIcons.IN_OUT_ICON
    ContractType.IN_UNIT -> ChainIcons.IN_UNIT_ICON
    ContractType.UNIT_OUT -> ChainIcons.UNIT_OUT_ICON
    ContractType.UNIT_UNIT -> ChainIcons.UNIT_UNIT_ICON
}

fun InputValueContractType.toIcon(): Icon = when (this) {
    InputValueContractType.IN -> ChainIcons.IN_IMAGE_ICON
    InputValueContractType.UNIT -> ChainIcons.UNIT_IMAGE_ICON
}

fun OutputValueContractType.toIcon(): Icon = when (this) {
    OutputValueContractType.OUT -> ChainIcons.OUT_IMAGE_ICON
    OutputValueContractType.UNIT -> ChainIcons.UNIT_IMAGE_ICON
}


fun iconForContractTypes(first: ContractType?, second: ContractType?): Icon? =
    when {
        first != null && second != null -> CompoundIcon(first.inputValue.toIcon(), second.output.toIcon())
        first != null && second == null -> first.toIcon()
        first == null && second != null -> second.toIcon()
        else -> null
    }

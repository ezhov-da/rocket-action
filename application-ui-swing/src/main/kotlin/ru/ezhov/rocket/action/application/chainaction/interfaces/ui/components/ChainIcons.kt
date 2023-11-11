package ru.ezhov.rocket.action.application.chainaction.interfaces.ui.components

import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicActionEngine
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicActionSource
import ru.ezhov.rocket.action.application.chainaction.domain.model.ContractType
import ru.ezhov.rocket.action.application.chainaction.domain.model.InputValueContractType
import ru.ezhov.rocket.action.application.chainaction.domain.model.OutputValueContractType
import ru.ezhov.rocket.action.application.resources.Icons
import tips4java.CompoundIcon
import javax.swing.Icon

object ChainIcons {
    val IN_IMAGE_ICON_8x8 = Icons.Standard.CHEVRON_RIGHT_8x8
    val OUT_IMAGE_ICON_8x8 = Icons.Standard.CHEVRON_LEFT_8x8
    val UNIT_IMAGE_ICON_8x8 = Icons.Standard.MEDIA_RECORD_8x8

    val IN_IMAGE_ICON_16x16 = Icons.Standard.CHEVRON_RIGHT_16x16
    val OUT_IMAGE_ICON_16x16 = Icons.Standard.CHEVRON_LEFT_16x16
    val UNIT_IMAGE_ICON_16x16 = Icons.Standard.MEDIA_RECORD_16x16

    val IN_OUT_ICON_8x8 = CompoundIcon(IN_IMAGE_ICON_8x8, OUT_IMAGE_ICON_8x8)
    val IN_UNIT_ICON_8x8 = CompoundIcon(IN_IMAGE_ICON_8x8, UNIT_IMAGE_ICON_8x8)
    val UNIT_OUT_ICON_8x8 = CompoundIcon(UNIT_IMAGE_ICON_8x8, OUT_IMAGE_ICON_8x8)
    val UNIT_UNIT_ICON_8x8 = CompoundIcon(UNIT_IMAGE_ICON_8x8, UNIT_IMAGE_ICON_8x8)

    val IN_OUT_ICON_16x16 = CompoundIcon(IN_IMAGE_ICON_16x16, OUT_IMAGE_ICON_16x16)
    val IN_UNIT_ICON_16x16 = CompoundIcon(IN_IMAGE_ICON_16x16, UNIT_IMAGE_ICON_16x16)
    val UNIT_OUT_ICON_16x16 = CompoundIcon(UNIT_IMAGE_ICON_16x16, OUT_IMAGE_ICON_16x16)
    val UNIT_UNIT_ICON_16x16 = CompoundIcon(UNIT_IMAGE_ICON_16x16, UNIT_IMAGE_ICON_16x16)
}

fun ContractType.toIcon8x8(): Icon = when (this) {
    ContractType.IN_OUT -> ChainIcons.IN_OUT_ICON_8x8
    ContractType.IN_UNIT -> ChainIcons.IN_UNIT_ICON_8x8
    ContractType.UNIT_OUT -> ChainIcons.UNIT_OUT_ICON_8x8
    ContractType.UNIT_UNIT -> ChainIcons.UNIT_UNIT_ICON_8x8
}

fun AtomicActionEngine.toIcon8x8(): Icon = when (this) {
    AtomicActionEngine.KOTLIN -> Icons.Advanced.KOTLIN_8x8
    AtomicActionEngine.GROOVY -> Icons.Advanced.GROOVY_8x8
}

fun AtomicActionSource.toIcon8x8(): Icon = when (this) {
    AtomicActionSource.TEXT -> Icons.Standard.ALIGN_CENTER_8x8
    AtomicActionSource.FILE -> Icons.Standard.FILE_8x8
}

fun InputValueContractType.toIcon8x8(): Icon = when (this) {
    InputValueContractType.IN -> ChainIcons.IN_IMAGE_ICON_8x8
    InputValueContractType.UNIT -> ChainIcons.UNIT_IMAGE_ICON_8x8
}

fun OutputValueContractType.toIcon8x8(): Icon = when (this) {
    OutputValueContractType.OUT -> ChainIcons.OUT_IMAGE_ICON_8x8
    OutputValueContractType.UNIT -> ChainIcons.UNIT_IMAGE_ICON_8x8
}


fun iconForContractTypes(first: ContractType?, second: ContractType?): Icon? =
    when {
        first != null && second != null -> CompoundIcon(first.inputValue.toIcon8x8(), second.output.toIcon8x8())
        first != null && second == null -> first.toIcon8x8()
        first == null && second != null -> second.toIcon8x8()
        else -> null
    }

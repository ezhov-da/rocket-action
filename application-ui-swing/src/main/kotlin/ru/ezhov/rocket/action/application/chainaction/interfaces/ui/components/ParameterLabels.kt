package ru.ezhov.rocket.action.application.chainaction.interfaces.ui.components

import tips4java.CompoundIcon
import javax.swing.ImageIcon
import javax.swing.JLabel

private val inImageIcon = ImageIcon(InOutLabel::class.java.getResource("/icons/open-iconic/png/chevron-right.png"))
private val outImageIcon = ImageIcon(InOutLabel::class.java.getResource("/icons/open-iconic/png/chevron-left.png"))
private val unitImageIcon = ImageIcon(InOutLabel::class.java.getResource("/icons/open-iconic/png/media-record.png"))

val inOutIcon = CompoundIcon(inImageIcon, outImageIcon)
val inUnitIcon = CompoundIcon(inImageIcon, unitImageIcon)
val unitOutIcon = CompoundIcon(unitImageIcon, outImageIcon)
val unitUnitIcon = CompoundIcon(unitImageIcon, unitImageIcon)

class InOutLabel : JLabel(CompoundIcon(inImageIcon, outImageIcon))

class InUnitLabel : JLabel(CompoundIcon(inImageIcon, unitImageIcon))

class UnitOutLabel : JLabel(CompoundIcon(unitImageIcon, outImageIcon))

class UnitUnitLabel : JLabel(CompoundIcon(unitImageIcon, unitImageIcon))

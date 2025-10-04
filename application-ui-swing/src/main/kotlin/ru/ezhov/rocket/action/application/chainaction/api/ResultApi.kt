package ru.ezhov.rocket.action.application.chainaction.api

class ResultApi(
    val result: Any?,

    val uiWidth: Int? = null,
    val uiHeight: Int? = null,

    val uiWidthPercent: Double? = null,
    val uiHeightPercent: Double? = null,

    val isUseParentLocation: Boolean = true
) {
    companion object {
        @JvmStatic
        fun withSize(
            result: Any?,
            uiWidth: Int,
            uiHeight: Int,
            isUseParentLocation: Boolean
        ): ResultApi = ResultApi(
            result = result,
            uiWidth = uiWidth,
            uiHeight = uiHeight,
            isUseParentLocation = isUseParentLocation
        )

        @JvmStatic
        fun withPercentSize(
            result: Any?,
            uiWidthPercent: Double,
            uiHeightPercent: Double,
            isUseParentLocation: Boolean
        ): ResultApi = ResultApi(
            result = result,
            uiWidthPercent = uiWidthPercent,
            uiHeightPercent = uiHeightPercent,
            isUseParentLocation = isUseParentLocation,
        )
    }
}

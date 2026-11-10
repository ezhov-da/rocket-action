package ru.ezhov.rocket.action.plugin.noteonfile

class CalculatePointService {
    fun calculate(delimiter: String, text: String): List<Point> {
        val delimitersInfo = delimitersInfo(delimiter = delimiter, text = text)
        val partsByDelimiter = partsByDelimiter(delimiter = delimiter, text = text)
        return join(delimitersInfo, partsByDelimiter)
    }

    private fun delimitersInfo(delimiter: String, text: String): List<DelimiterInfo> {
        val mutableListDelimiterInfo = mutableListOf<DelimiterInfo>()
        val stepValue = delimiter.length
        var textLength = text.length
        var counter = 0
        while (counter <= textLength) {
            val nextPosition = counter + stepValue
            if (nextPosition > textLength) break
            val part = text.subSequence(counter, nextPosition)
            if (part == delimiter) {
                mutableListDelimiterInfo.add(DelimiterInfo(counter))
            }
            counter++
        }

        return mutableListDelimiterInfo
    }

    private fun join(
        delimitersInfo: List<DelimiterInfo>,
        partsByDelimiter: List<String>
    ): List<Point> =
        if (partsByDelimiter.size > delimitersInfo.size) {
            delimitersInfo.mapIndexed { index, delimiterInfo ->
                Point.of(index = delimiterInfo.index, text = partsByDelimiter[index + 1].trim())
            }
                .toMutableList()
                .apply {
                    add(index = 0, element = Point.of(index = 0, text = partsByDelimiter[0].trim()))
                }
        } else {
            delimitersInfo.mapIndexed { index, delimiterInfo ->
                Point.of(index = delimiterInfo.index, text = partsByDelimiter[index].trim())
            }
        }

    private fun partsByDelimiter(delimiter: String, text: String) = text.split(delimiter)

    private data class DelimiterInfo(val index: Int)
}

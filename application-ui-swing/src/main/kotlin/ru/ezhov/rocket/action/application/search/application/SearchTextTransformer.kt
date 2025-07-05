package ru.ezhov.rocket.action.application.search.application

import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Service

private val map = mapOf(
    "ё" to "`",
    "й" to "q",
    "ц" to "w",
    "у" to "e",
    "к" to "r",
    "е" to "t",
    "н" to "y",
    "г" to "u",
    "ш" to "i",
    "щ" to "o",
    "з" to "p",
    "х" to "[",
    "ъ" to "]",
    "ф" to "a",
    "ы" to "s",
    "в" to "d",
    "а" to "f",
    "п" to "g",
    "р" to "h",
    "о" to "j",
    "л" to "k",
    "д" to "l",
    "ж" to ";",
    "э" to "'",
    "я" to "z",
    "ч" to "x",
    "с" to "c",
    "м" to "v",
    "и" to "b",
    "т" to "n",
    "ь" to "m",
    "б" to ",",
    "ю" to ".",
    "\"" to "/",
)

@Service
class SearchTextTransformer : InitializingBean {
    companion object {
        var INSTANCE: SearchTextTransformer? = null
    }

    override fun afterPropertiesSet() {
        INSTANCE = this
    }

    fun transformedText(input: String): List<String> {
        val origin = input.lowercase().trim()
        if (origin.isEmpty()) return listOf(input)

        var replace = origin
        val first = replace.first()
        val resultMap = if (map.containsKey(first.toString())) {
            map
        } else {
            map.map { (k, v) -> v to k }.toMap()
        }

        resultMap.forEach { (k, v) ->
            replace = replace.replace(k, v)
            replace = replace.replace(k, v)
        }

        return listOf(origin, replace).distinct()
    }


}

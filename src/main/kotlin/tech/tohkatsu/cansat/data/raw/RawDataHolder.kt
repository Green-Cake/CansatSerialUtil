package tech.tohkatsu.cansat.data.raw

import tech.tohkatsu.cansat.data.ParsedData

@ExperimentalUnsignedTypes
class RawDataHolder(val data: List<RawData>, val isCorrect: Boolean) {

    override fun toString() = buildString {

        data.forEachIndexed { index, it ->

            append(it.value)
            if(index != data.lastIndex)
                append(", ")

        }

    }

    fun parse() = ParsedData(
        data[0].value as Float,
        data[1].value as Float, data[2].value as Float, data[3].value as Float,
        data[4].value as Float, data[5].value as Float, data[6].value as Float
    )

}
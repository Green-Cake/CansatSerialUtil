package tech.tohkatsu.cansat

@ExperimentalUnsignedTypes
class DataHolder(val data: List<Any>, val isCorrect: Boolean) {

    override fun toString() = buildString {

        data.forEachIndexed { index, it ->

            append(it)
            if(index != data.lastIndex)
                append(", ")

        }

    }

}
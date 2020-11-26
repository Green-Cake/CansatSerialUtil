package tech.tohkatsu.cansat

@ExperimentalUnsignedTypes
class DataHolder(val data: List<Data>, val isCorrect: Boolean) {

    override fun toString() = buildString {

        data.forEachIndexed { index, it ->

            append(it.data)
            if(index != data.lastIndex)
                append(", ")

        }

    }

}
package tech.tohkatsu.cansat.data.raw

@ExperimentalUnsignedTypes
class RawDataHolder(val data: List<RawData>, val isCorrect: Boolean) {

    override fun toString() = buildString {

        data.forEachIndexed { index, it ->

            append(it.value)
            if(index != data.lastIndex)
                append(", ")

        }

    }

    fun parse() {

        val time = data[0].value as Float

        val ax = data[1].value as Float
        val ay = data[2].value as Float
        val az = data[3].value as Float

        val gx = data[4].value as Float
        val gy = data[5].value as Float
        val gz = data[6].value as Float

    }

}
package tech.tohkatsu.cansat.data.raw

@ExperimentalUnsignedTypes
@Suppress("EXPERIMENTAL_UNSIGNED_LITERALS")
enum class RawDataType(val value: UByte, val bytes: Int) {
    I32(0x01u, 2),
    FLOAT(0x02u, 2)
    ;

    companion object {

        fun getByIDValue(id: UByte) = values().firstOrNull { it.value == id }

    }

    fun convertFromBytes(bytes: List<UByte>): Any = when(this) {
        I32 -> bytes[0] * 0x100u + bytes[1]
        FLOAT -> Float.fromBits((bytes[0] * 0x100u + bytes[1]).toInt())
    }

}
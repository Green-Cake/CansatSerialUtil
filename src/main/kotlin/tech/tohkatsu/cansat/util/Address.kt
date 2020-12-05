package tech.tohkatsu.cansat.util

@ExperimentalUnsignedTypes
enum class Address(val value: UByte) {

    PC(0x01u),
    ALPHA(0x02u),
    BETA(0x03u)

}
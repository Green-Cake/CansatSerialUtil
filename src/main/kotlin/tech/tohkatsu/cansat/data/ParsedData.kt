package tech.tohkatsu.cansat.data

data class ParsedData(
    val elapsedTimeSec: Float,

    //accelerator
    val ax: Float,
    val ay: Float,
    val az: Float,

    //gyro
    val gx: Float,
    val gy: Float,
    val gz: Float,
)
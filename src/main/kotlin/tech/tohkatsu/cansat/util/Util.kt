package tech.tohkatsu.cansat.util

fun <T> List<T>.deepEquals(other: List<T>): Boolean {

    if(this === other)
        return true

    if(this.size != other.size)
        return false

    for(i in 0..this.lastIndex)
        if(this[i] != other[i])
            return false

    return true

}

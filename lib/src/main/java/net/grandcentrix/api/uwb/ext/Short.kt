package net.grandcentrix.api.uwb.ext

fun Short.toByteArray(): ByteArray {
    val result = ByteArray(2)
    result[1] = (this.toInt() and 0xff).toByte()
    result[0] = (this.toInt() shr 8 and 0xff).toByte()
    return result
}

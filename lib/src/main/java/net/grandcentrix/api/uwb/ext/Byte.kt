package net.grandcentrix.api.uwb.ext

fun Byte.toByteArray(): ByteArray {
    val result = ByteArray(1)
    result[0] = (this.toInt() and 0xff).toByte()
    return result
}

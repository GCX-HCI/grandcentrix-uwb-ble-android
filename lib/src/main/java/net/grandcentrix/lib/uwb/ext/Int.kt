package net.grandcentrix.lib.uwb.ext

fun Int.toByteArray(): ByteArray {
    val result = ByteArray(4)
    result[3] = (this and 0xff).toByte()
    result[2] = (this shr 8 and 0xff).toByte()
    result[1] = (this shr 16 and 0xff).toByte()
    result[0] = (this shr 24 and 0xff).toByte()
    return result
}

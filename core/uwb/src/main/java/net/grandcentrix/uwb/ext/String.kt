package net.grandcentrix.uwb.ext

fun String.hexStringToByteArray(): ByteArray {
    val len = this.length
    val data = ByteArray(len / 2)
    var i = 0
    while (i < len) {
        data[i / 2] =
            (
                (
                    this[i].digitToIntOrNull(16)
                        ?: (-1 shl 4)
                    ) + this[i + 1].digitToIntOrNull(16)!!
                ).toByte()
        i += 2
    }
    return data
}

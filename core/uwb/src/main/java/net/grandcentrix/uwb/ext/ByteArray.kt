package net.grandcentrix.uwb.ext

fun ByteArray.toShort(): Short {
    return when (this.size) {
        1 -> {
            (this[0].toInt() and 0xFF).toShort()
        }
        2 -> {
            ((this[0].toInt() and 0xFF shl 8) + (this[1].toInt() and 0xFF)).toShort()
        }
        else -> {
            throw IndexOutOfBoundsException()
        }
    }
}

fun ByteArray.toInt(): Int {
    return when (this.size) {
        1 -> {
            this[0].toInt() and 0xFF
        }
        2 -> {
            (this[0].toInt() and 0xFF shl 8) + (this[1].toInt() and 0xFF)
        }
        3 -> {
            (this[0].toInt() and 0xFF shl 16) + (this[1].toInt() and 0xFF shl 8) + (this[2].toInt() and 0xFF)
        }
        4 -> {
            (this[0].toInt() shl 24) + (this[1].toInt() and 0xFF shl 16) + (this[2].toInt() and 0xFF shl 8) + (this[3].toInt() and 0xFF)
        }
        else -> {
            throw IndexOutOfBoundsException()
        }
    }
}

fun ByteArray.toByte(): Byte {
    return if (this.size == 1) {
        (this[0].toInt() and 0xFF).toByte()
    } else {
        throw IndexOutOfBoundsException()
    }
}

fun ByteArray.toHexString(): String {
    val hexChars = "0123456789ABCDEF"
    val hexString = StringBuilder(this.size * 2)

    for (byte in this) {
        val intValue = byte.toInt() and 0xFF
        hexString.append(hexChars[intValue shr 4 and 0x0F])
        hexString.append(hexChars[intValue and 0x0F])
    }

    return hexString.toString()
}

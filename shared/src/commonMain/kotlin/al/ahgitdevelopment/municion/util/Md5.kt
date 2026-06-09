package al.ahgitdevelopment.municion.util

/**
 * Pure-Kotlin MD5 (RFC 1321), multiplatform.
 *
 * Exists solely to reproduce `java.util.UUID.nameUUIDFromBytes` byte-for-byte on every
 * platform, so [al.ahgitdevelopment.municion.data.sync.SyncIdGenerator.deterministicSyncId]
 * keeps generating the SAME ids the Android app generated. See golden-vector tests.
 */

private val MD5_S =
    intArrayOf(
        7,
        12,
        17,
        22,
        7,
        12,
        17,
        22,
        7,
        12,
        17,
        22,
        7,
        12,
        17,
        22,
        5,
        9,
        14,
        20,
        5,
        9,
        14,
        20,
        5,
        9,
        14,
        20,
        5,
        9,
        14,
        20,
        4,
        11,
        16,
        23,
        4,
        11,
        16,
        23,
        4,
        11,
        16,
        23,
        4,
        11,
        16,
        23,
        6,
        10,
        15,
        21,
        6,
        10,
        15,
        21,
        6,
        10,
        15,
        21,
        6,
        10,
        15,
        21,
    )

private val MD5_K =
    intArrayOf(
        0xd76aa478.toInt(),
        0xe8c7b756.toInt(),
        0x242070db,
        0xc1bdceee.toInt(),
        0xf57c0faf.toInt(),
        0x4787c62a,
        0xa8304613.toInt(),
        0xfd469501.toInt(),
        0x698098d8,
        0x8b44f7af.toInt(),
        0xffff5bb1.toInt(),
        0x895cd7be.toInt(),
        0x6b901122,
        0xfd987193.toInt(),
        0xa679438e.toInt(),
        0x49b40821,
        0xf61e2562.toInt(),
        0xc040b340.toInt(),
        0x265e5a51,
        0xe9b6c7aa.toInt(),
        0xd62f105d.toInt(),
        0x02441453,
        0xd8a1e681.toInt(),
        0xe7d3fbc8.toInt(),
        0x21e1cde6,
        0xc33707d6.toInt(),
        0xf4d50d87.toInt(),
        0x455a14ed,
        0xa9e3e905.toInt(),
        0xfcefa3f8.toInt(),
        0x676f02d9,
        0x8d2a4c8a.toInt(),
        0xfffa3942.toInt(),
        0x8771f681.toInt(),
        0x6d9d6122,
        0xfde5380c.toInt(),
        0xa4beea44.toInt(),
        0x4bdecfa9,
        0xf6bb4b60.toInt(),
        0xbebfbc70.toInt(),
        0x289b7ec6,
        0xeaa127fa.toInt(),
        0xd4ef3085.toInt(),
        0x04881d05,
        0xd9d4d039.toInt(),
        0xe6db99e5.toInt(),
        0x1fa27cf8,
        0xc4ac5665.toInt(),
        0xf4292244.toInt(),
        0x432aff97,
        0xab9423a7.toInt(),
        0xfc93a039.toInt(),
        0x655b59c3,
        0x8f0ccc92.toInt(),
        0xffeff47d.toInt(),
        0x85845dd1.toInt(),
        0x6fa87e4f,
        0xfe2ce6e0.toInt(),
        0xa3014314.toInt(),
        0x4e0811a1,
        0xf7537e82.toInt(),
        0xbd3af235.toInt(),
        0x2ad7d2bb,
        0xeb86d391.toInt(),
    )

/** Returns the 16-byte MD5 digest of [message]. */
fun md5(message: ByteArray): ByteArray {
    var a0 = 0x67452301
    var b0 = 0xefcdab89.toInt()
    var c0 = 0x98badcfe.toInt()
    var d0 = 0x10325476

    val msgLen = message.size
    val totalLen = (((msgLen + 8) / 64) + 1) * 64
    val msg = ByteArray(totalLen)
    message.copyInto(msg)
    msg[msgLen] = 0x80.toByte()
    val bitLen = msgLen.toLong() * 8L
    for (i in 0 until 8) {
        msg[totalLen - 8 + i] = ((bitLen ushr (8 * i)) and 0xff).toByte()
    }

    var offset = 0
    while (offset < totalLen) {
        val m = IntArray(16)
        for (i in 0 until 16) {
            val j = offset + i * 4
            m[i] = (msg[j].toInt() and 0xff) or
                ((msg[j + 1].toInt() and 0xff) shl 8) or
                ((msg[j + 2].toInt() and 0xff) shl 16) or
                ((msg[j + 3].toInt() and 0xff) shl 24)
        }

        var a = a0
        var b = b0
        var c = c0
        var d = d0
        for (i in 0 until 64) {
            val f: Int
            val g: Int
            when {
                i < 16 -> {
                    f = (b and c) or (b.inv() and d)
                    g = i
                }
                i < 32 -> {
                    f = (d and b) or (d.inv() and c)
                    g = (5 * i + 1) % 16
                }
                i < 48 -> {
                    f = b xor c xor d
                    g = (3 * i + 5) % 16
                }
                else -> {
                    f = c xor (b or d.inv())
                    g = (7 * i) % 16
                }
            }
            val tmp = d
            d = c
            c = b
            b += (a + f + MD5_K[i] + m[g]).rotateLeft(MD5_S[i])
            a = tmp
        }
        a0 += a
        b0 += b
        c0 += c
        d0 += d
        offset += 64
    }

    val out = ByteArray(16)
    val words = intArrayOf(a0, b0, c0, d0)
    for (w in 0 until 4) {
        for (i in 0 until 4) {
            out[w * 4 + i] = ((words[w] ushr (8 * i)) and 0xff).toByte()
        }
    }
    return out
}

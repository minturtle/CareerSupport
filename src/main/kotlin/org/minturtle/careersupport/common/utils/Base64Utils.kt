package org.minturtle.careersupport.common.utils

import java.nio.charset.StandardCharsets
import java.util.*

object Base64Utils {

    fun encode(str: String): String {
        return Base64.getEncoder().encodeToString(str.toByteArray(StandardCharsets.UTF_8))
    }

    fun decode(str: String?): String {
        val decodedBytes = Base64.getDecoder().decode(str)
        return String(decodedBytes, StandardCharsets.UTF_8)
    }
}
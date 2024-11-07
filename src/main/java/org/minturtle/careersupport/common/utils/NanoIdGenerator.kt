package org.minturtle.careersupport.common.utils

import com.aventrix.jnanoid.jnanoid.NanoIdUtils
import java.util.*

object NanoIdGenerator {

    fun createNanoId(idSize: Int = 10): String {
        val random = Random()
        return NanoIdUtils.randomNanoId(random, NanoIdUtils.DEFAULT_ALPHABET, idSize)
    }

}
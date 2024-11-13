package org.minturtle.careersupport.common.dto

import org.minturtle.careersupport.common.utils.NanoIdGenerator

class RequestInfo {

     val requestId: String = NanoIdGenerator.createNanoId(8)
}
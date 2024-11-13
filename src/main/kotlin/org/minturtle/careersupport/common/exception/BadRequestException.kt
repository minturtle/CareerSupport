package org.minturtle.careersupport.common.exception

class BadRequestException : RuntimeException {

    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
}
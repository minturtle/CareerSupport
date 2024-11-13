package org.minturtle.careersupport.common.exception

class InternalServerException : RuntimeException {

    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
}
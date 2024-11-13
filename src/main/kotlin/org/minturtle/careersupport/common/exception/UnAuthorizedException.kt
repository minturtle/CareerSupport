package org.minturtle.careersupport.common.exception


class UnAuthorizedException : RuntimeException {

    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
}
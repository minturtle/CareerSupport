package org.minturtle.careersupport.common.controller

import org.minturtle.careersupport.common.dto.GlobalErrorResponse
import org.minturtle.careersupport.common.exception.ConflictException
import org.minturtle.careersupport.common.exception.UnAuthorizedException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalControllerAdvice {

    private val log = LoggerFactory.getLogger(GlobalControllerAdvice::class.java)

    @ExceptionHandler(UnAuthorizedException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun handleUnAuthorizedException(e: UnAuthorizedException) = GlobalErrorResponse(e.message)


    @ExceptionHandler(ConflictException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleConflictException(e: ConflictException) = GlobalErrorResponse(e.message)

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleUnExpectedException(e: Exception): GlobalErrorResponse<String> {
        log.warn("UNEXPECTED EXCEPTION THROWS : ${e.javaClass.simpleName} ${e.stackTrace}" )
        return GlobalErrorResponse(e.message)
    }
}
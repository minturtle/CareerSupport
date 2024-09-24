package org.minturtle.careersupport.common.controller;


import org.minturtle.careersupport.common.dto.GlobalErrorResponse;
import org.minturtle.careersupport.common.exception.ConflictException;
import org.minturtle.careersupport.common.exception.UnAuthorizedException;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalControllerAdvice {


    @ExceptionHandler({UnAuthorizedException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public GlobalErrorResponse<String> handleUnAuthorizedException(UnAuthorizedException e){
        return new GlobalErrorResponse<>(e.getMessage());
    }

    @ExceptionHandler({ConflictException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public GlobalErrorResponse<String> handleConflictException(ConflictException e){
        return new GlobalErrorResponse<>(e.getMessage());
    }

    @ExceptionHandler({Exception.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public GlobalErrorResponse<String> handleUnExpectedException(Exception e){
        return new GlobalErrorResponse<>(e.getMessage());
    }
}
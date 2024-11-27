package org.minturtle.careersupport.common.dto


data class GlobalErrorResponse<T> (
    private val message: T? = null
)
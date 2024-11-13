package org.minturtle.careersupport.common.dto

data class CursoredResponse<T>(
    val cursor: String? = null,
    val data: List<T>
)
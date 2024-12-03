package org.minturtle.careersupport.common.entity

import org.minturtle.careersupport.common.utils.NanoIdGenerator
import org.springframework.data.annotation.Id


open class BaseEntity{
    @Id
    var id: String = NanoIdGenerator.createNanoId(10)
        protected set

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BaseEntity

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }


}
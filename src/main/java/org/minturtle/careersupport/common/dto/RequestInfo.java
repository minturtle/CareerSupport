package org.minturtle.careersupport.common.dto;


import lombok.Getter;
import org.minturtle.careersupport.common.utils.NanoIdGenerator;

@Getter
public class RequestInfo {

    private final String requestId;

    public RequestInfo() {
        this.requestId = NanoIdGenerator.createNanoId(8);
    }
}
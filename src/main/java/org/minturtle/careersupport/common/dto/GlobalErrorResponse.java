package org.minturtle.careersupport.common.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GlobalErrorResponse<T> {

    private T message;

}
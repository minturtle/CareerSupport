package org.minturtle.careersupport.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;


@Builder
@AllArgsConstructor
@Getter
public class CursoredResponse<T> {

    private final String cursor;
    private final List<T> data;
}

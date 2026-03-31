package com.brovko.SsuBench.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class JwtResponseDto {
    private String jwt;

    public JwtResponseDto(String jwt) {
        this.jwt = jwt;
    }

}

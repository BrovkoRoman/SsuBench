package com.brovko.SsuBench.dto;

public class JwtResponseDto {
    private String jwt;

    public JwtResponseDto(String jwt) {
        this.jwt = jwt;
    }

    public String getJwt() {
        return jwt;
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }
}

package com.brovko.SsuBench.dto;

import com.brovko.SsuBench.entity.User;
import com.brovko.SsuBench.exception.ValidationException;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserRegisterRequestDto {
    private String login;

    private String password;

    private User.Role role;

    public void validate() {
        if (login == null) {
            throw new ValidationException("login is null");
        }

        if (password == null) {
            throw new ValidationException("password is null");
        }

        if (role == null) {
            throw new ValidationException("role is null");
        }
    }
}

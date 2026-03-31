package com.brovko.SsuBench.dto;

import com.brovko.SsuBench.entity.User;
import com.brovko.SsuBench.exception.ValidationException;

public class UserLoginRequestDto {
    private String login;

    private String password;

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void validate() {
        if (login == null) {
            throw new ValidationException("login is null");
        }

        if (password == null) {
            throw new ValidationException("password is null");
        }
    }
}

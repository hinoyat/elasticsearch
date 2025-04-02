package com.c202.user.auth.model.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginRequestDto {

    private String username;
    private String password;
}

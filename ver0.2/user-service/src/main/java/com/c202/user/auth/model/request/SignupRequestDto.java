package com.c202.user.auth.model.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignupRequestDto {

    private String username;
    private String password;
    private String nickname;
    private String birthDate;
    private Integer iconSeq;
}

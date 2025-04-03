package com.c202.user.user.model.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateUserRequestDto {

    private String nickname;
    private String password;
    private String birthDate;
    private Integer iconSeq;
}

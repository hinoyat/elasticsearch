package com.c202.user.user.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UserProfileDto {
    private Integer userSeq;
    private String nickname;
    private Integer iconSeq;
}

package com.c202.user.user.model.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateIntroductionDto {

    private String introduction;
}

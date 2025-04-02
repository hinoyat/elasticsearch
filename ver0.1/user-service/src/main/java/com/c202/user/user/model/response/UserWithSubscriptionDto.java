package com.c202.user.user.model.response;

import com.c202.user.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserWithSubscriptionDto {

    private Integer userSeq;
    private String username;
    private String nickname;
    private String birthDate;
    private String introduction;
    private Integer iconSeq;
    private String isSubscribed; // Y or N

    public static UserWithSubscriptionDto from(User user, String isSubscribed) {
        return UserWithSubscriptionDto.builder()
                .userSeq(user.getUserSeq())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .birthDate(user.getBirthDate())
                .introduction(user.getIntroduction())
                .iconSeq(user.getIconSeq())
                .isSubscribed(isSubscribed)
                .build();
    }
}

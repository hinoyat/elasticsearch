package com.c202.user.user.model.response;

import com.c202.user.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponseDto {

    private Integer userSeq;
    private String username;
    private String nickname;
    private String birthDate;
    private String introduction;
    private Integer iconSeq;
    
    public static UserResponseDto toDto(User user) {
        if (user == null) {
            return empty();
        }
        return UserResponseDto.builder()
                .userSeq(user.getUserSeq())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .birthDate(user.getBirthDate())
                .introduction(user.getIntroduction())
                .iconSeq(user.getIconSeq())
                .build();
    }

    public static UserResponseDto empty() {
        return UserResponseDto.builder()
                .userSeq(null)
                .username(null)
                .nickname(null)
                .birthDate(null)
                .introduction(null)
                .iconSeq(null)
                .build();
    }
}

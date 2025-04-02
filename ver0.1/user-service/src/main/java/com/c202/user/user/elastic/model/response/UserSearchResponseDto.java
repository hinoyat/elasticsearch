package com.c202.user.user.elastic.model.response;

import com.c202.user.user.elastic.document.UserDocument;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSearchResponseDto {
    private Integer userSeq;
    private String username;
    private String nickname;
    private String birthDate;
    private String introduction;
    private Integer iconSeq;

    public static UserSearchResponseDto fromDocument(UserDocument document) {
        return UserSearchResponseDto.builder()
                .userSeq(document.getUserSeq())
                .username(document.getUsername())
                .nickname(document.getNickname())
                .birthDate(document.getBirthDate())
                .introduction(document.getIntroduction())
                .iconSeq(document.getIconSeq())
                .build();
    }
}

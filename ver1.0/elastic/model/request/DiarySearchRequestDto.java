package com.c202.diary.elastic.model.request;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiarySearchRequestDto {
    private String keyword;
    private boolean searchTitle;
    private boolean searchContent;
    private boolean searchTag;
    private boolean currentUserOnly;
    private boolean useCurrentUser; // 지금 로그인 되어있는 사용자 userSeq 사용여부

    private Integer targetUserSeq; // useCurrentUser 값이 false일 때 넣어줘야 함
    private Integer page;
    private Integer size;
}
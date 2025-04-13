package com.c202.diary.elastic.model.response;

import com.c202.diary.elastic.document.DiaryDocument;
import com.c202.diary.tag.model.response.TagResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiarySearchListResponseDto {
    private Integer diarySeq;
    private Integer userSeq;
    private String title;
    private String content;
    private String dreamDate;
    private String isPublic;
    private String mainEmotion;
    private List<TagResponseDto> tags;

    public static DiarySearchListResponseDto fromDocument(DiaryDocument document, String emotionName, List<TagResponseDto> tags) {
        return DiarySearchListResponseDto.builder()
                .diarySeq(document.getDiarySeq())
                .userSeq(document.getUserSeq())
                .title(document.getTitle())
                .content(document.getContent())
                .dreamDate(document.getDreamDate())
                .isPublic(document.getIsPublic())
                .mainEmotion(emotionName)
                .tags(tags)
                .build();
    }
}
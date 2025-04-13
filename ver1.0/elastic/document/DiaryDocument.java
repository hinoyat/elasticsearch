package com.c202.diary.elastic.document;

import org.springframework.data.annotation.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.util.List;

@Document(indexName = "diary")
@Setting(settingPath = "/elasticsearch/diary-settings.json")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiaryDocument {

    @Id
    @Field(type = FieldType.Integer)
    private Integer diarySeq;

    @Field(type = FieldType.Integer)
    private Integer userSeq;

    @Field(type = FieldType.Text, analyzer = "korean", searchAnalyzer = "korean_search")
    private String title;

    @Field(type = FieldType.Text, analyzer = "korean", searchAnalyzer = "korean_search")
    private String content;

    @Field(type = FieldType.Text, analyzer = "korean", searchAnalyzer = "korean_search")
    private List<String> tags;

    @Field(type = FieldType.Keyword)
    private String isPublic;

    @Field(type = FieldType.Keyword)
    private String isDeleted;

    @Field(type = FieldType.Keyword)
    private String dreamDate;

    @Field(type = FieldType.Integer)
    private Integer emotionSeq;
}

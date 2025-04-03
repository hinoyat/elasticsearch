package com.c202.user.user.elastic.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

@Document(indexName = "user")
@Setting(settingPath = "/elasticsearch/user-settings.json")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDocument {

    @Id
    @Field(type = FieldType.Integer)
    private Integer userSeq;

    @Field(type = FieldType.Text, analyzer = "korean")
    private String username;

    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "korean"),
            otherFields = {
                    @InnerField(suffix = "ngram", type = FieldType.Text, analyzer = "korean_ngram")
            }
    )
    private String nickname;

    @Field(type = FieldType.Keyword)
    private String isDeleted;

    @Field(type = FieldType.Keyword)
    private String birthDate;

    @Field(type = FieldType.Integer)
    private Integer iconSeq;

    @Field(type = FieldType.Text, analyzer = "korean")
    private String introduction;
}
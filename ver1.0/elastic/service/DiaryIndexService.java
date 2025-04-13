package com.c202.diary.elastic.service;

import com.c202.diary.diary.entity.Diary;
import com.c202.diary.elastic.document.DiaryDocument;
import com.c202.diary.elastic.repository.DiarySearchRepository;
import com.c202.diary.tag.entity.DiaryTag;
import com.c202.diary.tag.repository.DiaryTagRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiaryIndexService {

    private final DiarySearchRepository diarySearchRepository;
    private final DiaryTagRepository diaryTagRepository;

    @Transactional
    public void indexDiary(Diary diary) {
        // 항상 최신 태그 목록을 조회
        List<String> tags = diaryTagRepository.findByDiary(diary).stream()
                .map(diaryTag -> diaryTag.getTag().getName())
                .collect(Collectors.toList());

        DiaryDocument document = DiaryDocument.builder()
                .diarySeq(diary.getDiarySeq())
                .userSeq(diary.getUserSeq())
                .title(diary.getTitle())
                .content(diary.getContent())
                .tags(tags)
                .dreamDate(diary.getDreamDate())
                .isPublic(diary.getIsPublic())
                .isDeleted(diary.getIsDeleted())
                .emotionSeq(diary.getEmotionSeq())
                .build();

        diarySearchRepository.save(document);
    }

    public void deleteDiaryIndex(Integer diarySeq) {
        diarySearchRepository.deleteById(diarySeq);
    }

    private DiaryDocument convertToDocument(Diary diary) {
        // 태그 목록 추출
        List<String> tags = diary.getDiaryTags().stream()
                .map(diaryTag -> diaryTag.getTag().getName())
                .collect(Collectors.toList());

        return DiaryDocument.builder()
                .diarySeq(diary.getDiarySeq())
                .userSeq(diary.getUserSeq())
                .title(diary.getTitle())
                .content(diary.getContent())
                .tags(tags)
                .dreamDate(diary.getDreamDate())
                .isPublic(diary.getIsPublic())
                .isDeleted(diary.getIsDeleted())
                .emotionSeq(diary.getEmotionSeq())
                .build();
    }
}
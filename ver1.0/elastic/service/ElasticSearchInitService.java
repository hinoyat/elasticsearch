package com.c202.diary.elastic.service;

import com.c202.diary.diary.entity.Diary;
import com.c202.diary.diary.repository.DiaryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ElasticSearchInitService {

    private final DiaryRepository diaryRepository;
    private final DiaryIndexService diaryIndexService;

    @Transactional
    @EventListener(ApplicationReadyEvent.class)
    public void initializeElasticSearch() {
        List<Diary> diaries = diaryRepository.findByIsDeleted("N");

        for (Diary diary : diaries) {
            diaryIndexService.indexDiary(diary);
        }
    }
}
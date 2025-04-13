package com.c202.diary.elastic.controller;

import com.c202.diary.elastic.model.request.DiarySearchRequestDto;
import com.c202.diary.elastic.model.response.DiarySearchListResponseDto;
import com.c202.diary.elastic.model.response.PageResponseDto;
import com.c202.diary.elastic.service.DiarySearchService;
import com.c202.dto.ResponseDto;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class DiarySearchController {

    private final DiarySearchService diarySearchService;

    @GetMapping("/diaries/search")
    public ResponseEntity<ResponseDto<PageResponseDto<DiarySearchListResponseDto>>> searchDiaries(
            @RequestHeader("X-User-Seq") @NotNull Integer userSeq,
            @ModelAttribute DiarySearchRequestDto requestDto) {
        log.info("Diary search request: {}", requestDto);
        log.info("Diary search request: {}", requestDto.toString());

        Page<DiarySearchListResponseDto> results;
        if (!requestDto.isUseCurrentUser() && requestDto.getTargetUserSeq() != null) {
            results = diarySearchService.searchDiaries(requestDto, requestDto.getTargetUserSeq());
        } else {
            results = diarySearchService.searchDiaries(requestDto, userSeq);
        }
        PageResponseDto<DiarySearchListResponseDto> pageResponse = PageResponseDto.from(results);
        return ResponseEntity.ok(ResponseDto.success(200, "일기 검색 완료", pageResponse));
    }
}
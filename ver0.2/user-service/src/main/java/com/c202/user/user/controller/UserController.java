package com.c202.user.user.controller;

import com.c202.dto.ResponseDto;
import com.c202.user.user.model.request.UpdateIntroductionDto;
import com.c202.user.user.model.request.UpdateUserRequestDto;
import com.c202.user.user.model.response.UserProfileDto;
import com.c202.user.user.model.response.UserResponseDto;
import com.c202.user.user.model.response.UserWithSubscriptionDto;
import com.c202.user.user.service.UserService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    // 사용자 정보 조회
    @GetMapping("/me")
    public ResponseEntity<ResponseDto<UserResponseDto>> getMyInfo(
            @RequestHeader("X-User-Seq") @NotNull Integer userSeq) {
        UserResponseDto user = userService.getUserByUserSeq(userSeq);
        return ResponseEntity.ok(ResponseDto.success(200, "사용자 정보 조회 성공", user));
    }

    // 유저 조회 API
    @GetMapping("/seq/{otherSeq}")
    public ResponseEntity<ResponseDto<UserWithSubscriptionDto>> getUserByUserSeq(
            @PathVariable Integer otherSeq,
            @RequestHeader("X-User-Seq") @NotNull Integer userSeq) {

        UserWithSubscriptionDto user = userService.getUserByUserSeqWithSubscription(otherSeq, userSeq);
        return ResponseEntity.ok(ResponseDto.success(200, "사용자 정보 및 구독 여부 조회 성공", user));
    }

    // 유저 조회 API
    @GetMapping("/name/{username}")
    public ResponseEntity<ResponseDto<UserWithSubscriptionDto>> getUserByUsernameWithSubscription(
            @PathVariable String username,
            @RequestHeader("X-User-Seq") @NotNull Integer userSeq) {

        UserWithSubscriptionDto user = userService.getUserByUsernameWithSubscription(username, userSeq);
        return ResponseEntity.ok(ResponseDto.success(200, "사용자 정보 및 구독 여부 조회 성공", user));
    }


    // 사용자 정보 수정
    @PutMapping("/me")
    public ResponseEntity<ResponseDto<UserResponseDto>> updateUser(
            @RequestHeader("X-User-Seq") @NotNull Integer userSeq,
            @RequestBody UpdateUserRequestDto requestDto) {
        UserResponseDto updatedUser = userService.updateUser(userSeq, requestDto);
        return ResponseEntity.ok(ResponseDto.success(200, "사용자 정보 수정 성공", updatedUser));
    }

    // 회원 탈퇴
    @DeleteMapping("/me")
    public ResponseEntity<ResponseDto<Void>> deleteUser(
            @RequestHeader("X-User-Seq")  @NotNull Integer userSeq) {
        userService.deleteUser(userSeq);
        return ResponseEntity.ok(ResponseDto.success(204, "회원 탈퇴 성공"));
    }

    @PutMapping("/me/intro")
    public ResponseEntity<ResponseDto<UserResponseDto>> updateIntroduction(
            @RequestHeader("X-User-Seq") @NotNull Integer userSeq,
            @RequestBody UpdateIntroductionDto requestDto) {
        return ResponseEntity.ok(ResponseDto.success(201, "자기소개 수정 완료", userService.updateIntroduction(userSeq, requestDto)));
    }

    @GetMapping("/birthdate")
    public String getUserBirthDate(@RequestHeader("X-User-Seq") Integer userSeq){
        return userService.getUserBirthDate(userSeq);
    }

    @PostMapping("/profiles")
    public ResponseEntity<ResponseDto<List<UserProfileDto>>> getUserProfiles(@RequestBody List<Integer> userSeqList) {
        List<UserProfileDto> profiles = userService.getUserProfiles(userSeqList);
        return ResponseEntity.ok(ResponseDto.success(200, "유저 프로필 목록 조회 성공", profiles));
    }

    @GetMapping("/random")
    public ResponseEntity<ResponseDto<UserResponseDto>> getRandomUser() {
        UserResponseDto randomUser = userService.getRandomUser();
        return ResponseEntity.ok(ResponseDto.success(200, "랜덤 사용자 조회 성공", randomUser));
    }

}
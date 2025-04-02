package com.c202.user.auth.controller;

import com.c202.dto.ResponseDto;
import com.c202.exception.types.UnauthorizedException;
import com.c202.user.auth.service.AuthService;
import com.c202.user.auth.model.request.LoginRequestDto;
import com.c202.user.auth.model.request.SignupRequestDto;
import com.c202.user.user.model.response.UserResponseDto;
import com.c202.user.auth.jwt.JwtTokenProvider;
import com.c202.user.auth.jwt.TokenDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/register")
    public ResponseEntity<ResponseDto<UserResponseDto>> register(
            @RequestBody SignupRequestDto requestDto) {
        UserResponseDto user = authService.register(requestDto);
        return ResponseEntity.status(201).body(ResponseDto.success(201, "회원가입 성공", user));
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<ResponseDto<Map<String, String>>> login(
            @RequestBody LoginRequestDto requestDto,
            HttpServletResponse response) {

        TokenDto.TokenResponseDto tokens = authService.login(requestDto);

        jwtTokenProvider.addRefreshTokenToCookie(response, tokens.getRefreshToken());

        Map<String, String> tokenResponse = Map.of("accessToken", tokens.getAccessToken());
        response.addHeader("Authorization", "Bearer " + tokens.getAccessToken());

        return ResponseEntity.ok(ResponseDto.success(200, "로그인이 성공했습니다.", tokenResponse));
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<ResponseDto<Void>> logout(
            @RequestHeader("X-User-Seq") @NotNull Integer userSeq,
            HttpServletRequest request,
            HttpServletResponse response) {

        authService.logout(request, response, userSeq);

        return ResponseEntity.ok(ResponseDto.success(200, "로그아웃이 성공했습니다."));
    }

    @GetMapping("/check-username/{username}")
    public ResponseEntity<ResponseDto<Map<String, Boolean>>> checkUsernameAvailability(
            @PathVariable String username) {
        boolean isAvailable = authService.isUsernameAvailable(username);
        return ResponseEntity.ok(ResponseDto.success(200, isAvailable ? "사용 가능한 아이디입니다." : "이미 사용 중인 아이디입니다."));
    }

    @GetMapping("/check-nickname/{nickname}")
    public ResponseEntity<ResponseDto<Map<String, Boolean>>> checkNicknameAvailability(
            @PathVariable String nickname) {
        boolean isAvailable = authService.isNicknameAvailable(nickname);
        return ResponseEntity.ok(ResponseDto.success(200, isAvailable ? "사용 가능한 닉네임입니다." : "이미 사용 중인 닉네임입니다."));
    }

    // 토큰 갱신
    @PostMapping("/refresh")
    public ResponseEntity<ResponseDto<Map<String, String>>> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response) {

        String refreshToken = jwtTokenProvider.extractRefreshTokenFromCookie(request);

        if (refreshToken == null) {
            throw new UnauthorizedException("리프레시 토큰이 없습니다.");
        }

        String newAccessToken = jwtTokenProvider.refreshAccessToken(refreshToken);

        Map<String, String> tokenResponse = Map.of("accessToken", newAccessToken);

        return ResponseEntity.ok(ResponseDto.success(200, "토큰이 갱신되었습니다.", tokenResponse));
    }

}

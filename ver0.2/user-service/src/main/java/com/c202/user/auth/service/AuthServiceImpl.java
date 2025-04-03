package com.c202.user.auth.service;

import com.c202.exception.types.*;
import com.c202.user.user.elastic.service.UserIndexService;
import com.c202.user.user.entity.User;
import com.c202.user.auth.model.request.LoginRequestDto;
import com.c202.user.auth.model.request.SignupRequestDto;
import com.c202.user.user.model.response.UserResponseDto;
import com.c202.user.user.repository.UserRepository;
import com.c202.user.auth.jwt.JwtTokenProvider;
import com.c202.user.auth.jwt.TokenDto;
import com.c202.user.auth.jwt.refreshtoken.RefreshTokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserIndexService userIndexService;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd HHmmss");

    @Override
    @Transactional
    public UserResponseDto register(SignupRequestDto request) {
        // 중복 검사
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("이미 사용 중인 아이디입니다.");
        }

        if (userRepository.existsByNickname(request.getNickname())) {
            throw new ConflictException("이미 사용 중인 닉네임입니다.");
        }

        String now = LocalDateTime.now().format(DATE_TIME_FORMATTER);

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .birthDate(request.getBirthDate())
                .createdAt(now)
                .updatedAt(now)
                .isDeleted("N")
                .iconSeq(request.getIconSeq())
                .build();

        User savedUser = userRepository.save(user);

        userIndexService.indexUser(savedUser);

        return UserResponseDto.toDto(savedUser);
    }

    // 로그인 메소드 - 액세스 토큰과 리프레시 토큰 모두 발급
    @Override
    public TokenDto.TokenResponseDto login(LoginRequestDto request) {
        // 삭제된 계정인지 확인
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));

        // 비밀번호 체크 (passwordEncoder.matches() 사용)
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("잘못된 비밀번호입니다.");
        }
        if ("Y".equals(user.getIsDeleted())) {
            throw new BadRequestException("탈퇴한 계정입니다.");
        }

        // 액세스 토큰과 리프레시 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(user.getUsername(), user.getUserSeq());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getUsername(), user.getUserSeq());

        // 토큰 응답 객체 생성 및 반환
        return TokenDto.TokenResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    // 로그아웃
    @Override
    @Transactional
    public void logout(HttpServletRequest request, HttpServletResponse response, Integer userSeq) {
        log.debug("사용자 ID={}의 로그아웃 처리 시작", userSeq);

        try {
            // 엑세스 토큰 블랙리스트 처리
            String accessToken = extractTokenFromRequest(request);
            if (StringUtils.hasText(accessToken)) {
                jwtTokenProvider.blacklistToken(accessToken);
                log.info("엑세스 토큰 블랙리스트 처리 완료: 사용자 ID={}", userSeq);
            }

            // 리프레시 토큰 제거
            refreshTokenRepository.deleteByUserSeq(userSeq);
            log.debug("리프레시 토큰 삭제 완료: 사용자 ID={}", userSeq);

            // 쿠키 삭제
            jwtTokenProvider.deleteRefreshTokenCookie(response);

        } catch (Exception e) {
            log.error("로그아웃 처리 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    @Override
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }

    @Override
    public boolean isNicknameAvailable(String nickname) {
        return !userRepository.existsByNickname(nickname);
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}

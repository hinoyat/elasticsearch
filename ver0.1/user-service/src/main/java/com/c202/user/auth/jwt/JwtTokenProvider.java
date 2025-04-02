package com.c202.user.auth.jwt;

import com.c202.exception.types.*;
import com.c202.user.auth.jwt.blacklist.TokenBlacklistService;
import com.c202.user.auth.jwt.refreshtoken.RefreshToken;
import com.c202.user.auth.jwt.refreshtoken.RefreshTokenRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {

    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.access-token-validity-in-ms}")
    private long accessTokenValidity;

    @Value("${jwt.refresh-token-validity-in-ms}")
    private long refreshTokenValidity;

    private Key key;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final RefreshTokenRepository refreshTokenRepository;

    private final TokenBlacklistService tokenBlacklistService;

    @PostConstruct
    public void init() {
        // 시크릿 키를 Base64로 인코딩하여 JWT 서명용 키 생성
        byte[] keyBytes = Base64.getDecoder().decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String createAccessToken(String username, int userSeq) {
        // JWT 클레임 설정 - 사용자 식별자와 권한 정보 담기
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("userSeq", userSeq);
        claims.put("type", "access"); // 토큰 타입 지정

        // 현재 시간과 만료 시간 설정
        Date now = new Date();
        Date validity = new Date(now.getTime() + accessTokenValidity);

        // JWT 토큰 생성 및 반환
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken(String username, int userSeq) {
        // JWT 클레임 설정
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("userSeq", userSeq);
        claims.put("type", "refresh"); // 토큰 타입 지정

        // 현재 시간과 만료 시간 설정
        Date now = new Date();
        Date validity = new Date(now.getTime() + refreshTokenValidity);

        // JWT 리프레시 토큰 생성
        String refreshToken = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        // 만료 시간을 문자열로 변환
        String expiryDate = LocalDateTime.now().plusNanos(refreshTokenValidity * 1000000).format(DATE_FORMATTER);

        // DB에 리프레시 토큰 저장 또는 업데이트
        Optional<RefreshToken> existingToken = refreshTokenRepository.findByUserSeq(userSeq);

        if (existingToken.isPresent()) {
            // 기존 토큰이 있으면 업데이트
            RefreshToken tokenEntity = existingToken.get();
            tokenEntity.updateToken(refreshToken, expiryDate);
            refreshTokenRepository.save(tokenEntity);
        } else {
            // 기존 토큰이 없으면 새로 생성
            RefreshToken tokenEntity = RefreshToken.builder()
                    .userSeq(userSeq)
                    .token(refreshToken)
                    .expiryDate(expiryDate)
                    .build();
            refreshTokenRepository.save(tokenEntity);
        }

        return refreshToken;
    }

    // 리프레시 토큰을 쿠키에 설정
    public void addRefreshTokenToCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie("refresh_token", refreshToken);
        cookie.setHttpOnly(true);  // JavaScript에서 접근 불가능하게 설정
        cookie.setSecure(true);    // HTTPS에서만 전송 (운영 환경에서 활성화)
        cookie.setPath("/");  // 쿠키 경로 설정
        cookie.setMaxAge((int) (refreshTokenValidity / 1000));  // 초 단위로 변환

        response.addCookie(cookie);
    }

    // 쿠키에서 리프레시 토큰 추출
    public String extractRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refresh_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    // 리프레시 토큰 쿠키 삭제
    public void deleteRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("refresh_token", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);  // 즉시 만료

        response.addCookie(cookie);
    }

    // 토큰에서 사용자명 추출
    public String getUsername(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
    }

    // 토큰에서 사용자 ID 추출
    public int getUserSeq(String token) {
        return ((int) Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().get("userSeq"));
    }

    // 토큰 유형 확인 (액세스 또는 리프레시)
    public String getTokenType(String token) {
        return (String) Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().get("type");
    }

    // 토큰 만료 시간 추출
    public Date getTokenExpiration(String token) {
        return (Date) Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().getExpiration();
    }

    // 블랙리스트 추가
    public void blacklistToken(String token) {
        try {
            Date expirationDate = getTokenExpiration(token);

            tokenBlacklistService.addToBlacklist(token, expirationDate);
            log.info("Blacklisted token: " + token.substring(0, 10));

        } catch ( Exception e ) {
            log.error("토큰 추가 오류: {}", e.getMessage());
        }
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            if (tokenBlacklistService.isBlacklisted(token)) {
                log.warn("블랙리스트에 등록된 토큰입니다: {}", token.substring(0, 10));
                return false;
            }

            // 토큰 파싱 시도
            Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);

            // 만료 확인
            boolean isValid = !claims.getBody().getExpiration().before(new Date());
            if (isValid) {
                log.debug("유효한 토큰: {}, 사용자: {}", token.substring(0, 10), claims.getBody().getSubject());
            } else {
                log.warn("만료된 토큰: {}", token.substring(0, 10));
            }
            return isValid;
        } catch (ExpiredJwtException e) {
            log.warn("만료된 JWT 토큰: {}", e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 JWT 토큰: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            log.error("잘못된 형식의 JWT 토큰: {}", e.getMessage());
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("잘못된 JWT 토큰: {}", e.getMessage());
            return false;
        }
    }


    // 리프레시 토큰으로 새 액세스 토큰 발급
    public String refreshAccessToken(String refreshToken) {
        // 리프레시 토큰 유효성 검증
        if (!validateToken(refreshToken)) {
            throw new UnauthorizedException("유효하지 않은 리프레시 토큰입니다.");
        }

        // 토큰 타입 확인
        String tokenType = getTokenType(refreshToken);
        if (!"refresh".equals(tokenType)) {
            throw new BadRequestException("리프레시 토큰이 아닙니다.");
        }

        // DB에 저장된 리프레시 토큰 확인
        Optional<RefreshToken> savedToken = refreshTokenRepository.findByToken(refreshToken);
        if (savedToken.isEmpty()) {
            throw new UnauthorizedException("저장된 리프레시 토큰을 찾을 수 없습니다.");
        }

        // 토큰에서 사용자 정보 추출
        String username = getUsername(refreshToken);
        int userSeq = getUserSeq(refreshToken);

        // 새 액세스 토큰 생성 및 반환
        return createAccessToken(username, userSeq);
    }

}
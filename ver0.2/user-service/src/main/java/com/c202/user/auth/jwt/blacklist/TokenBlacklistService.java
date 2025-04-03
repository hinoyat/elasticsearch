package com.c202.user.auth.jwt.blacklist;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final String BLACKLIST_PREFIX = "token:blacklist:";


    // 토큰 블랙리스트 추가
    public void addToBlacklist(String token, Date expirationTime) {
        try {
            long ttl = expirationTime.getTime() - System.currentTimeMillis();
            if (ttl > 0) {
                String tokenHash = DigestUtils.sha256Hex(token);
                String key = BLACKLIST_PREFIX + tokenHash;
                redisTemplate.opsForValue().set(key, "blacklisted", ttl, TimeUnit.MILLISECONDS);
                log.info("토큰이 블랙리스트에 추가되었습니다 토큰해쉬{}. 남은 시간: {}ms",tokenHash, ttl);
            } else {
                log.info("이미 만료된 토큰입니다. 블랙리스트에 추가하지 않습니다.");
            }
        } catch (Exception e) {
            log.error("토큰 블랙리스트 추가 중 오류 발생: {}", e.getMessage(), e);
        }
    }


    
    // 토큰 블랙리스트 확인
    public boolean isBlacklisted(String token) {
        try {
            String tokenHash = DigestUtils.sha256Hex(token);
            String key = BLACKLIST_PREFIX + tokenHash;
            boolean isBlacklisted = Boolean.TRUE.equals(redisTemplate.hasKey(key));
            if (isBlacklisted) {
                log.debug("토큰이 블랙리스트에 있습니다: {}", token.substring(0, 10));
            }
            return isBlacklisted;
        } catch (Exception e) {
            log.error("토큰 블랙리스트 확인 중 오류 발생: {}", e.getMessage(), e);
            return false;
        }
    }

}

package com.c202.user.auth.jwt.refreshtoken;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByUserSeq(Integer userSeq);

    void deleteByUserSeq(Integer userSeq);
}
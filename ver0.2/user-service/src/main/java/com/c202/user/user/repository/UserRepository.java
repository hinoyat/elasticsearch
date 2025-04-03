package com.c202.user.user.repository;

import com.c202.user.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUserSeq(Integer userSeq);
    Optional<User> findByUsernameAndIsDeleted(String username, String isDeleted);
    boolean existsByUsername(String username);
    boolean existsByNickname(String nickname);
    Optional<User> findByUsername(String username);
    List<User> findByUserSeqInAndIsDeleted(List<Integer> userSeqList, String isDeleted);

    @Query(value = "SELECT * FROM users WHERE isDeleted = 'N' ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Optional<User> findRandomActiveUser();

    List<User> findByIsDeleted(String isDeleted);
}

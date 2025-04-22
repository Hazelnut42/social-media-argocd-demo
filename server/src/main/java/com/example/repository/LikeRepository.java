package com.example.repository;

import com.example.model.Like;

import java.sql.Timestamp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByUserIdAndPostId(Long userId, Long postId);

    long countByPostIdAndType(Long postId, Like.LikeType type);
    @Modifying
    @Query(value = "INSERT IGNORE INTO likes (user_id, post_id, type, created_at) " +
        "VALUES (:userId, :postId, :type, :createdAt)", nativeQuery = true)
    int insertIfNotExists(
        @Param("userId") Long userId,
        @Param("postId") Long postId,
        @Param("type") String type,
        @Param("createdAt") Timestamp createdAt
    );

    Like save(Like entity);

    void delete(Like entity);
}
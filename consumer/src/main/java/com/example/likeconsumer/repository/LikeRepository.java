package com.example.likeconsumer.repository;

import com.example.likeconsumer.entity.Like;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<Like, Long> {
  Optional<Like> findByUserIdAndPostId(Long userId, Long postId);

  long countByPostIdAndType(Long postId, Like.LikeType type);
} 
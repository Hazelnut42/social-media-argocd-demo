package com.example.service;

import com.example.model.Like;
import com.example.payload.LikeEvent;
import com.example.repository.LikeRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class LikeService {
  @Autowired
  private LikeRepository likeRepository;
  @Autowired
  private RedisTemplate<String, String> redisTemplate;
  @Autowired
  private KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper = new ObjectMapper();


  // Redis key patterns
  private static final String POST_LIKES_KEY = "post:%d:likes";
  private static final String POST_DISLIKES_KEY = "post:%d:dislikes";
  private static final String USER_POST_ACTION_KEY = "user:%d:post:%d";
  private static final long CACHE_EXPIRE_TIME = 24 * 3600L; // 1day

  @PostConstruct
  public void init() {
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  }

  public Map<String, Long> getLikeCounts(Long postId) {
    String likeKey = String.format(POST_LIKES_KEY, postId);
    String dislikeKey = String.format(POST_DISLIKES_KEY, postId);

    Map<String, Long> counts = new HashMap<>();

    // 尝试从Redis获取数据
    String likesStr = redisTemplate.opsForValue().get(likeKey);
    String dislikesStr = redisTemplate.opsForValue().get(dislikeKey);

    if (likesStr == null || dislikesStr == null) {
      // 如果缓存没有，从数据库获取
      log.info("Cache miss for post {} likes/dislikes, fetching from database", postId);

      // 从数据库获取点赞数
      long likesCount = likeRepository.countByPostIdAndType(postId, Like.LikeType.LIKE);
      long dislikesCount = likeRepository.countByPostIdAndType(postId, Like.LikeType.DISLIKE);

      // 更新缓存
      try {
        redisTemplate.opsForValue()
            .set(likeKey, String.valueOf(likesCount), CACHE_EXPIRE_TIME,
                TimeUnit.SECONDS);
        redisTemplate.opsForValue()
            .set(dislikeKey, String.valueOf(dislikesCount), CACHE_EXPIRE_TIME,
                TimeUnit.SECONDS);

        log.info("Updated cache for post {}: likes={}, dislikes={}", postId, likesCount,
            dislikesCount);

        // 返回数据库中的计数
        counts.put("likes", likesCount);
        counts.put("dislikes", dislikesCount);
      } catch (Exception e) {
        log.error("Error updating Redis cache for post {}", postId, e);
        // 即使缓存更新失败，仍然返回数据库中的计数
        counts.put("likes", likesCount);
        counts.put("dislikes", dislikesCount);
      }
    } else {
      // 使用缓存中的数据
      counts.put("likes", Long.parseLong(likesStr));
      counts.put("dislikes", Long.parseLong(dislikesStr));
      log.debug("Cache hit for post {}: likes={}, dislikes={}", postId, likesStr,
          dislikesStr);
    }

    return counts;
  }


  public void refreshLikeCountsCache(Long postId) {
    String likeKey = String.format(POST_LIKES_KEY, postId);
    String dislikeKey = String.format(POST_DISLIKES_KEY, postId);

    try {
      long likesCount = likeRepository.countByPostIdAndType(postId, Like.LikeType.LIKE);
      long dislikesCount = likeRepository.countByPostIdAndType(postId, Like.LikeType.DISLIKE);

      redisTemplate.opsForValue()
          .set(likeKey, String.valueOf(likesCount), CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
      redisTemplate.opsForValue()
          .set(dislikeKey, String.valueOf(dislikesCount), CACHE_EXPIRE_TIME,
              TimeUnit.SECONDS);

      log.info("Manually refreshed cache for post {}: likes={}, dislikes={}", postId,
          likesCount, dislikesCount);
    } catch (Exception e) {
      log.error("Error refreshing Redis cache for post {}", postId, e);
    }
  }

  public void processLike(Long userId, Long postId, Like.LikeType type) {
    String userActionKey = String.format(USER_POST_ACTION_KEY, userId, postId);
    String likeKey = String.format(POST_LIKES_KEY, postId);
    String dislikeKey = String.format(POST_DISLIKES_KEY, postId);

    // 获取用户之前的操作
    String previousAction = redisTemplate.opsForValue().get(userActionKey);

    // 更新Redis
    if (previousAction != null) {
      // 如果之前有操作，先撤销之前的操作
      if (previousAction.equals("LIKE")) {
        redisTemplate.opsForValue().decrement(likeKey);
      } else if (previousAction.equals("DISLIKE")) {
        redisTemplate.opsForValue().decrement(dislikeKey);
      }
    }

    // 设置新的操作
    if (type == Like.LikeType.LIKE) {
      redisTemplate.opsForValue().increment(likeKey);
      redisTemplate.opsForValue().set(userActionKey, "LIKE");
    } else if (type == Like.LikeType.DISLIKE) {
      redisTemplate.opsForValue().increment(dislikeKey);
      redisTemplate.opsForValue().set(userActionKey, "DISLIKE");
    }

    // 发送Kafka消息
    try {
      LikeEvent event = new LikeEvent(userId, postId, type, LocalDateTime.now());
      String message = objectMapper.writeValueAsString(event);
      kafkaTemplate.send("likes-topic", message);
      log.info("Sent like event to Kafka: {}", message);
    } catch (JsonProcessingException e) {
      log.error("Error sending like event to Kafka", e);
    }
  }

  public void removeLike(Long userId, Long postId) {
    String userActionKey = String.format(USER_POST_ACTION_KEY, userId, postId);
    String likeKey = String.format(POST_LIKES_KEY, postId);
    String dislikeKey = String.format(POST_DISLIKES_KEY, postId);

    // 获取并删除之前的操作
    String previousAction = redisTemplate.opsForValue().get(userActionKey);
    if (previousAction != null) {
      if (previousAction.equals("LIKE")) {
        redisTemplate.opsForValue().decrement(likeKey);
      } else if (previousAction.equals("DISLIKE")) {
        redisTemplate.opsForValue().decrement(dislikeKey);
      }
      redisTemplate.delete(userActionKey);
    }

    // 发送Kafka消息
    try {
      LikeEvent event = new LikeEvent(userId, postId, null, LocalDateTime.now());
      String message = objectMapper.writeValueAsString(event);
      kafkaTemplate.send("likes-topic", message);
      log.info("Sent remove like event to Kafka: {}", message);
    } catch (JsonProcessingException e) {
      log.error("Error sending remove like event to Kafka", e);
    }
  }

  public String getUserAction(Long userId, Long postId) {
    String userActionKey = String.format(USER_POST_ACTION_KEY, userId, postId);
    return redisTemplate.opsForValue().get(userActionKey);
  }

  @Transactional
  public Like save(Like like) {
    return likeRepository.save(like);
  }

  @Transactional
  public void delete(Like like) {
    likeRepository.delete(like);
  }

  @Transactional
  public Optional<Like> findByUserIdAndPostId(Long userId, Long postId) {
    return likeRepository.findByUserIdAndPostId(userId, postId);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void handleLikeEvent(String message) throws JsonProcessingException {
    LikeEvent event = objectMapper.readValue(message, LikeEvent.class);
    log.info("Received like event: {}", message);

    // 查找是否存在之前的点赞记录
    Optional<Like> existingLike = findByUserIdAndPostId(
        event.getUserId(), event.getPostId());

    if (event.getType() == null) {
      // 删除操作
      existingLike.ifPresent(this::delete);
      log.info("Deleted like record for user {} on post {}",
          event.getUserId(), event.getPostId());
    } else {
      if (!existingLike.isPresent()) {
        // 更新或创建新的点赞记录
        Like like = existingLike.orElse(new Like());
        like.setUserId(event.getUserId());
        like.setPostId(event.getPostId());
        like.setType(event.getType());
        like.setCreatedAt(event.getTimestamp());

        Like save = save(like);
        log.info("Saved like record: {}", save);
      }
    }
  }
}
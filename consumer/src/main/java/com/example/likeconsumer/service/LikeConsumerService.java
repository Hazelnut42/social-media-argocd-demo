package com.example.likeconsumer.service;

import com.example.likeconsumer.entity.Like;
import com.example.likeconsumer.entity.LikeEvent;
import com.example.likeconsumer.repository.LikeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.Optional;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LikeConsumerService {

  @Autowired
  private LikeRepository likeRepository;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @PostConstruct
  public void init() {
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  }

  @KafkaListener( topics = "likes-topic",
      groupId = "${spring.kafka.consumer.group-id}")
  public void consumeLike(String message) {
    try {
      LikeEvent event = objectMapper.readValue(message, LikeEvent.class);
      log.info("Received like event: {}", message);

      // 查找是否存在之前的点赞记录
      Optional<Like> existingLike = likeRepository.findByUserIdAndPostId(
          event.getUserId(), event.getPostId());

      if (event.getType() == null) {
        // 删除操作
        existingLike.ifPresent(likeRepository::delete);
        log.info("Deleted like record for user {} on post {}",
            event.getUserId(), event.getPostId());
      } else {
        // 更新或创建新的点赞记录
        Like like = existingLike.orElse(new Like());
        like.setUserId(event.getUserId());
        like.setPostId(event.getPostId());
        like.setType(event.getType());
        like.setCreatedAt(event.getTimestamp());

        likeRepository.save(like);
        log.info("Saved like record: {}", like);
      }
    } catch (Exception e) {
      log.error("Error processing like event: {}", message, e);
    }
  }
}

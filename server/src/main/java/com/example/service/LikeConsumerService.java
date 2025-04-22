package com.example.service;

import com.example.model.Like;
import com.example.payload.LikeEvent;
import com.example.repository.LikeRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Timestamp;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

//@Component
@Slf4j
@RequiredArgsConstructor
public class LikeConsumerService {
  private final LikeService likeService;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @KafkaListener(
      topics = "likes-topic",
      groupId = "${spring.kafka.consumer.group-id}",
      containerFactory = "kafkaListenerContainerFactory"
  )
  public void handleLikeEvent(String message) throws JsonProcessingException {
    try {
      likeService.handleLikeEvent(message); // ✅ Spring 代理调用事务方法
    } catch (Exception e) {
      log.error("❌ Error processing like event: {}", message, e);
//      throw e;
    }
  }
}
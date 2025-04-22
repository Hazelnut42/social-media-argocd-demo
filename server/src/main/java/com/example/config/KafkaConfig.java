package com.example.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Created by Chelsea on 2025-04-20
 */
@Configuration
public class KafkaConfig {
  private final PlatformTransactionManager transactionManager;
  public KafkaConfig(PlatformTransactionManager transactionManager) {
    this.transactionManager = transactionManager;
  }
  @Bean
  public NewTopic likesTopic() {
    return TopicBuilder.name("likes-topic")
        .partitions(1)
        .replicas(1)
        .build();
  }



  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
      ConsumerFactory<String, String> consumerFactory) {

    ConcurrentKafkaListenerContainerFactory<String, String> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory);

    // ✅ 关键点：为 Kafka Listener 容器指定事务管理器
    factory.getContainerProperties().setTransactionManager(transactionManager);

    // ✅ 推荐的 ack 模式（每条消息一个事务）
    factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);

    return factory;
  }
}

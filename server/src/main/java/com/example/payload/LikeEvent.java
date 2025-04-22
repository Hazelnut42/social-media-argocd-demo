package com.example.payload;

import com.example.model.Like;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LikeEvent {
    private Long userId;
    private Long postId;
    private Like.LikeType type; // null 表示删除操作
    private LocalDateTime timestamp;
}
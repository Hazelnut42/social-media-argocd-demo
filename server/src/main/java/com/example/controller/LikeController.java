package com.example.controller;

import com.example.model.Like;
import com.example.security.UserPrincipal;
import com.example.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @PostMapping("/{postId}/like")
    public ResponseEntity<?> likePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        likeService.processLike(currentUser.getId(), postId, Like.LikeType.LIKE);
        return ResponseEntity.ok(Map.of("message", "Like processed"));
    }

    @PostMapping("/{postId}/dislike")
    public ResponseEntity<?> dislikePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        likeService.processLike(currentUser.getId(), postId, Like.LikeType.DISLIKE);
        return ResponseEntity.ok(Map.of("message", "Dislike processed"));
    }

    @DeleteMapping("/{postId}/like")
    public ResponseEntity<?> removeLike(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        likeService.removeLike(currentUser.getId(), postId);
        return ResponseEntity.ok(Map.of("message", "Like/Dislike removed"));
    }

    @GetMapping("/{postId}/likes")
    public ResponseEntity<?> getLikeCounts(@PathVariable Long postId) {
        Map<String, Long> counts = likeService.getLikeCounts(postId);
        return ResponseEntity.ok(counts);
    }

    @GetMapping("/{postId}/user-action")
    public ResponseEntity<?> getUserAction(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        String action = likeService.getUserAction(currentUser.getId(), postId);
        return ResponseEntity.ok(Map.of("action", action != null ? action : "NONE"));
    }
}
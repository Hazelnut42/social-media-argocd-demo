package com.example.service;

import com.example.model.Post;
import com.example.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    // 写操作使用主库
    @Transactional
    public Post createPost(Post post) {
        return postRepository.save(post);
    }

    // 读操作使用从库
    @Transactional(readOnly = true)
    public List<Post> getAllPosts() {
        return postRepository.findAllOrderByCreatedAtDesc();
    }

    // 读操作使用从库
    @Transactional(readOnly = true)
    public Post getPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
    }

    // 读操作使用从库
    @Transactional(readOnly = true)
    public List<Post> getPostsByUserId(Long userId) {
        return postRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    // 写操作使用主库
    @Transactional
    public Post updatePost(Post post) {
        return postRepository.save(post);
    }

    // 写操作使用主库
    @Transactional
    public void deletePost(Long postId) {
        postRepository.deleteById(postId);
    }
}
package org.ritika.cognitbackend.service.impl;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ritika.cognitbackend.dto.request.CreateCommentRequest;
import org.ritika.cognitbackend.dto.request.UpdateCommentRequest;
import org.ritika.cognitbackend.dto.response.CommentResponse;
import org.ritika.cognitbackend.entity.Comment;
import org.ritika.cognitbackend.entity.Post;
import org.ritika.cognitbackend.entity.User;
import org.ritika.cognitbackend.enums.Role;
import org.ritika.cognitbackend.exception.ResourceNotFoundException;
import org.ritika.cognitbackend.exception.UnauthorizedException;
import org.ritika.cognitbackend.repository.CommentRepository;
import org.ritika.cognitbackend.repository.PostRepository;
import org.ritika.cognitbackend.repository.UserRepository;
import org.ritika.cognitbackend.service.CommentService;
import org.ritika.cognitbackend.service.EmailService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public CommentResponse addComment(Long postId, CreateCommentRequest request, Long userId) {
        Post post = postRepository.findById(postId)
                .filter(p -> !p.getIsDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

        User author = userRepository.findById(userId)
                .filter(u -> !u.getIsDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Comment comment = Comment.builder()
                .post(post)
                .author(author)
                .body(request.getBody())
                .isDeleted(false)
                .build();

        Comment saved = commentRepository.save(comment);
        log.info("User {} added comment {} on post {}", userId, saved.getId(), postId);

        // Notify post author if commenter is different
        if (!post.getUser().getId().equals(userId)) {
            emailService.sendCommentNotification(post.getUser(), saved);
        }

        return CommentResponse.fromEntity(saved);
    }

    @Override
    public Page<CommentResponse> getCommentsForPost(Long postId, int page, int size) {
        // Verify post exists
        if (!postRepository.existsById(postId)) {
            throw new ResourceNotFoundException("Post", "id", postId);
        }
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());
        return commentRepository.findByPostIdAndIsDeletedFalse(postId, pageable)
                .map(CommentResponse::fromEntity);
    }

    @Override
    @Transactional
    public CommentResponse updateComment(Long commentId, UpdateCommentRequest request, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .filter(c -> !c.getIsDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new UnauthorizedException("You are not allowed to edit this comment");
        }

        comment.setBody(request.getBody());
        Comment updated = commentRepository.save(comment);
        log.info("User {} updated comment {}", userId, commentId);
        return CommentResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .filter(c -> !c.getIsDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));

        User user = userRepository.findById(userId)
                .filter(u -> !u.getIsDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        boolean isOwner = comment.getAuthor().getId().equals(userId);
        boolean isAdmin = user.getRole() == Role.ADMIN;
        boolean isPostAuthor = comment.getPost().getUser().getId().equals(userId);

        if (!isOwner && !isAdmin && !isPostAuthor) {
            throw new UnauthorizedException("You are not allowed to delete this comment");
        }

        comment.setIsDeleted(true);
        commentRepository.save(comment);
        log.info("User {} soft-deleted comment {}", userId, commentId);
    }
}

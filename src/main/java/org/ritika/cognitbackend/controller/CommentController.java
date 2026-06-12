package org.ritika.cognitbackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ritika.cognitbackend.dto.request.CreateCommentRequest;
import org.ritika.cognitbackend.dto.request.UpdateCommentRequest;
import org.ritika.cognitbackend.dto.response.CommentResponse;
import org.ritika.cognitbackend.entity.User;
import org.ritika.cognitbackend.service.CommentService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/posts/{postId}/comments")
@RequiredArgsConstructor
@Slf4j
public class CommentController {

    private final CommentService commentService;

    /**
     * Add a comment to a post. Any authenticated user can comment.
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable Long postId,
            @Valid @RequestBody CreateCommentRequest request,
            @AuthenticationPrincipal User user) {

        log.info("User {} adding comment to post {}", user.getId(), postId);
        CommentResponse response = commentService.addComment(postId, request, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get paginated comments for a post. Public endpoint.
     */
    @GetMapping
    public ResponseEntity<Page<CommentResponse>> getComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<CommentResponse> comments = commentService.getCommentsForPost(postId, page, size);
        return ResponseEntity.ok(comments);
    }

    /**
     * Update own comment.
     */
    @PutMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @Valid @RequestBody UpdateCommentRequest request,
            @AuthenticationPrincipal User user) {

        log.info("User {} updating comment {}", user.getId(), commentId);
        CommentResponse response = commentService.updateComment(commentId, request, user.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Soft-delete a comment. Allowed for: comment owner, post author, ADMIN.
     */
    @DeleteMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal User user) {

        log.info("User {} deleting comment {}", user.getId(), commentId);
        commentService.deleteComment(commentId, user.getId());
        return ResponseEntity.noContent().build();
    }

    /**
     * All non-deleted comments by the authenticated user, newest first.
     */
    @GetMapping("/api/v1/comments/mine")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<CommentResponse>> myComments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal User user) {

        Page<CommentResponse> result = commentService.getCommentsByUser(user.getId(), page, size);
        return ResponseEntity.ok(result);
    }
}

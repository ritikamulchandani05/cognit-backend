package org.ritika.cognitbackend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ritika.cognitbackend.dto.response.CommentResponse;
import org.ritika.cognitbackend.service.CommentService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin comment moderation — every comment across every post, not scoped to
 * a single post like CommentController's nested routes.
 */
@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
@Slf4j
public class AdminCommentController {

    private final CommentService commentService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<CommentResponse>> getAllComments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<CommentResponse> comments = commentService.getAllCommentsForAdmin(page, size);

        return ResponseEntity.ok(comments);
    }
}

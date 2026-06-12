package org.ritika.cognitbackend.service;

import org.ritika.cognitbackend.dto.request.CreateCommentRequest;
import org.ritika.cognitbackend.dto.request.UpdateCommentRequest;
import org.ritika.cognitbackend.dto.response.CommentResponse;
import org.springframework.data.domain.Page;

public interface CommentService {

    CommentResponse addComment(Long postId, CreateCommentRequest request, Long userId);

    Page<CommentResponse> getCommentsForPost(Long postId, int page, int size);

    CommentResponse updateComment(Long commentId, UpdateCommentRequest request, Long userId);

    void deleteComment(Long commentId, Long userId);

    Page<CommentResponse> getCommentsByUser(Long userId, int page, int size);
}

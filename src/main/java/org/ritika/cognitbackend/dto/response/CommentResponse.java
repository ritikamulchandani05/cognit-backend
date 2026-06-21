package org.ritika.cognitbackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ritika.cognitbackend.entity.Comment;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {

    private Long id;
    private Long postId;
    private String postTitle;
    private AuthorResponse author;
    private String body;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CommentResponse fromEntity(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .postId(comment.getPost().getId())
                .postTitle(comment.getPost().getTitle())
                .author(AuthorResponse.fromEntity(comment.getAuthor()))
                .body(comment.getBody())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}

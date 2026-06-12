package org.ritika.cognitbackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ritika.cognitbackend.entity.Post;
import org.ritika.cognitbackend.enums.PostStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {
    private Long id;
    private String title;
    private String content;
    private CategoryResponse category;
    private String slug;
    private String excerpt;
    private String featuredImageUrl;
    private PostStatus status;
    private Integer viewCount;
    private Integer likeCount;
    private AuthorResponse author;
    private List<TagResponse> tags;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long commentCount;
    private Boolean hasLiked;   // null for anonymous callers


    public static PostResponse fromEntity(Post post) {
        if(post == null) {
            return null;
        }

        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .category(CategoryResponse.fromEntity(post.getCategory()))
                .slug(post.getSlug())
                .excerpt(post.getExcerpt())
                .featuredImageUrl(post.getFeaturedImageUrl())
                .status(post.getStatus())
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .author(AuthorResponse.fromEntity(post.getUser()))
                .tags(post.getTags().stream()
                        .map(TagResponse::fromEntity)
                        .collect(Collectors.toList())
                )
                .publishedAt(post.getPublishedAt())
                .build();
    }
}


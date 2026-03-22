package org.ritika.cognitbackend.mapper;

import org.ritika.cognitbackend.dto.response.AuthorResponse;
import org.ritika.cognitbackend.dto.response.CategoryResponse;
import org.ritika.cognitbackend.dto.response.PostResponse;
import org.ritika.cognitbackend.dto.response.TagResponse;
import org.ritika.cognitbackend.entity.Post;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PostMapper {
    private final UserMapper userMapper;
    private final TagMapper tagMapper;
    private final CategoryMapper categoryMapper;

    @Autowired
    public PostMapper(UserMapper userMapper, TagMapper tagMapper, CategoryMapper categoryMapper) {
        this.userMapper = userMapper;
        this.tagMapper = tagMapper;
        this.categoryMapper = categoryMapper;
    }
    public PostResponse toResponse(Post post) {
        if(post == null) {
            return null;
        }

        PostResponse response = new PostResponse();
        response.setId(post.getId());
        response.setTitle(post.getTitle());
        response.setSlug(post.getSlug());
        response.setContent(post.getContent());
        response.setExcerpt(post.getExcerpt());
        response.setFeaturedImageUrl(post.getFeaturedImageUrl());
        response.setStatus(post.getStatus());
        response.setViewCount(post.getViewCount());
        response.setLikeCount(post.getLikeCount());
        response.setPublishedAt(post.getCreatedAt());
        response.setCreatedAt(post.getCreatedAt());
        response.setUpdatedAt(post.getUpdatedAt());

        response.setAuthor(userMapper.toAuthorResponse(post.getUser()));
        response.setCategory(categoryMapper.toResponse(post.getCategory()));
        response.setTags(tagMapper.toResponseList(post.getTags()));
        return response;
    }

    public List<PostResponse> toResponseList(List<Post> posts) {
        return posts.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    public PostResponse toResponseWithoutRelations(Post post) {
        if(post == null) {
            return null;
        }
        PostResponse response = new PostResponse();
        response.setId(post.getId());
        response.setTitle(post.getTitle());
        response.setSlug(post.getSlug());
        response.setContent(post.getContent());
        response.setExcerpt(post.getExcerpt());
        response.setFeaturedImageUrl(post.getFeaturedImageUrl());
        response.setStatus(post.getStatus());
        response.setViewCount(post.getViewCount());
        response.setLikeCount(post.getLikeCount());
        response.setPublishedAt(post.getCreatedAt());
        response.setCreatedAt(post.getCreatedAt());
        response.setUpdatedAt(post.getUpdatedAt());

        return response;
    }

    public static PostResponse toPostResponse(Post post) {
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

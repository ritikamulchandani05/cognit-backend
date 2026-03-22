package org.ritika.cognitbackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ritika.cognitbackend.enums.PostStatus;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostRequest {
    @NotBlank(message = "Title is required")
    @Size(min=2, max=200,message = "Name must be between 2 and 200 characters")
    private String title;

    @NotBlank(message = "Content is required")
    @Size(min=2, max=200,message = "Content must be at least 50 characters")
    private String content;

    @Size(max= 500, message = "Excerpt cannot exceed 500 characters")
    private String excerpt;

    private String featuredImageUrl;

    @NotNull(message = "Category is required")
    private Long categoryId;

    @Builder.Default
    private List<Long> tagIds = new ArrayList<>();

    @Builder.Default
    private PostStatus status = PostStatus.DRAFT;
}


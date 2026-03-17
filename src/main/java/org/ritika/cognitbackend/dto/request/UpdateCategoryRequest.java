package org.ritika.cognitbackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ritika.cognitbackend.enums.PostStatus;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCategoryRequest {
    @Size(min = 5, max = 200, message = "Title must be between 5 and 200 characters")
    private String title;

    @Size(min = 50, message = "Content must be at least 50")
    private String content;

    @Size(max = 500, message = "Excerpt cannot be exceeded 500 characters")
    private String excerpt;

    private String featuredImageUrl;

    private Long categoryId;

    private List<Long> tagsId;

    private PostStatus status;
}


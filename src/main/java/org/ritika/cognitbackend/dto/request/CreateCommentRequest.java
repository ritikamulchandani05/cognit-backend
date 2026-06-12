package org.ritika.cognitbackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateCommentRequest {

    @NotBlank(message = "Comment body must not be blank")
    @Size(min = 1, max = 5000, message = "Comment body must be between 1 and 5000 characters")
    private String body;
}

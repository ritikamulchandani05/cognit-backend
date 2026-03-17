package org.ritika.cognitbackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ritika.cognitbackend.entity.User;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthorResponse {

    private Long id;
    private String name;
    private String avtarUrl;

    public static AuthorResponse fromEntity(User user) {
        if(user == null) {
            return null;
        }

        return AuthorResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .avtarUrl(user.getAvatarUrl())
                .build();
    }
}


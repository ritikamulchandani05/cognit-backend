package org.ritika.cognitbackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;
    private String refreshToken;

    @Builder.Default
    private String tokenType = "Bearer";

    private Long expiresIn;
    private UserResponse user;

    private Boolean requires2fa = true;
    private String  tempToken;   // only populated when requires2fa == true
}

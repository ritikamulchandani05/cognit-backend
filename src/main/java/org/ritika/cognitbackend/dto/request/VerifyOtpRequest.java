package org.ritika.cognitbackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VerifyOtpRequest {

    @NotBlank
    private String tempToken;

    @NotBlank
    @Size(min = 6, max = 6)
    private String otp;
}
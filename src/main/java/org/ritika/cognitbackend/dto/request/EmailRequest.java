package org.ritika.cognitbackend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.ritika.cognitbackend.enums.EmailType;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest {

    private String to;
    private String subject;
    private String body;
    private EmailType type;
}
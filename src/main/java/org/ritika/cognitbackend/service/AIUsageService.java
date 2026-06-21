package org.ritika.cognitbackend.service;

import org.ritika.cognitbackend.dto.ai.AIUsageResponse;
import org.ritika.cognitbackend.enums.Role;

public interface AIUsageService {

    void checkAndIncrementUsage(Long userId, Role role);

    AIUsageResponse getUsage(Long userId, Role role);
}

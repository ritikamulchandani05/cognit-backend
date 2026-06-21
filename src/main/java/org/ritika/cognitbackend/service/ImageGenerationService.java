package org.ritika.cognitbackend.service;

import org.ritika.cognitbackend.dto.ai.GenerateImageRequest;
import org.ritika.cognitbackend.dto.ai.GenerateImageResponse;

public interface ImageGenerationService {
    GenerateImageResponse generateFeaturedImage(GenerateImageRequest request);
}

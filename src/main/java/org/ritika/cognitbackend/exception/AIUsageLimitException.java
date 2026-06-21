package org.ritika.cognitbackend.exception;

public class AIUsageLimitException extends RuntimeException {
    public AIUsageLimitException(String message) {
        super(message);
    }
}

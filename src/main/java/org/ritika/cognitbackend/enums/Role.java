package org.ritika.cognitbackend.enums;

public enum Role {
    SUBSCRIBER, // Free user - read posts, comment like
    AUTHOR, // Paid user - create posts, use AI (limited/unlimited)
    ADMIN // Full access - manage users, moderate content
}

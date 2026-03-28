package org.ritika.cognitbackend.security;

import lombok.RequiredArgsConstructor;
import org.ritika.cognitbackend.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Custom UserDetailsService implementation for Spring Security.
 * Loads user details from database for authentication.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    /**
     * Loads user details by user ID.
     * Used by JWT Filter when we have user ID from token.
     *
     * @param userId the user ID
     * @return UserDetails for the user
     */
    public UserDetails loadUserById(Long userId) {
        return userRepository.findById(userId)
                .filter(u ->!u.getIsDeleted())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));
    }
}

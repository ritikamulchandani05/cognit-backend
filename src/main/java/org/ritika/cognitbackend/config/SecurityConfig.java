package org.ritika.cognitbackend.config;

import lombok.RequiredArgsConstructor;
import org.ritika.cognitbackend.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**").permitAll()

                        // comments
                        .requestMatchers(HttpMethod.GET,"/api/v1/posts/*/comments/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/posts/*/comments/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/v1/posts/*/comments/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/posts/*/comments/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/posts/*/view").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/posts/*/like").authenticated()

                        // Admin moderation views
                        .requestMatchers(HttpMethod.GET, "/api/v1/posts/admin").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/comments").hasRole("ADMIN")

                        // Posts - reading published posts is public, all writes require a token + role
                        .requestMatchers(HttpMethod.GET,"/api/v1/posts/**").permitAll()
                        .requestMatchers(HttpMethod.POST,"/api/v1/posts/**").hasAnyRole("AUTHOR", "ADMIN")
                        .requestMatchers(HttpMethod.PUT,"/api/v1/posts/**").hasAnyRole("AUTHOR", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE,"/api/v1/posts/**").hasAnyRole("AUTHOR", "ADMIN")

                        // Categories - browsing is public; taxonomy management is ADMIN-only.
                        .requestMatchers(HttpMethod.GET,"/api/v1/categories/**").permitAll()
                        .requestMatchers(HttpMethod.POST,"/api/v1/categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,"/api/v1/categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE,"/api/v1/categories/**").hasRole("ADMIN")

                        // Tags - browsing is public; tag management is ADMIN-only.
                        .requestMatchers(HttpMethod.GET,"/api/v1/tags/**").permitAll()
                        .requestMatchers(HttpMethod.POST,"/api/v1/tags/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,"/api/v1/tags/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE,"/api/v1/tags/**").hasRole("ADMIN")

                        .requestMatchers("/swagger-ui/**","/v3/api-docs/**",
                                "/swagger-ui.html").permitAll()

                        // Uploaded media (featured images, avatars) is publicly viewable
                        .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

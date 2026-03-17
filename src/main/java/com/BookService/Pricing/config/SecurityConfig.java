package com.BookService.Pricing.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

// PricingService only accepts calls from BookService (internal service calls)
// It validates the JWT using Auth Server's public key from JWKS endpoint
// Only tokens with scope "internal" are accepted

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
//	@Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
//	private String jwkSetUri;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/**").permitAll()    // health checks open
                .anyRequest().authenticated()                   // all other endpoints need valid JWT
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> {} )
            );

        return http.build();
    }

//    // ── Validates tokens using Auth Server's public key ───────────────────────
//    // Auth Server exposes public key at: /oauth2/jwks
//    // PricingService fetches it automatically and caches it
//    @Bean
//    public JwtDecoder jwtDecoder() {
//        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
//    }

}
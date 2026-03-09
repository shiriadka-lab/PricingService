package com.BookService.Pricing;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//          .authorizeHttpRequests(expressionInterceptUrlRegistry ->
//            expressionInterceptUrlRegistry.requestMatchers(
//                    "/actuator/prometheus",
//                    "/actuator/health",
//                    "/actuator/info"
//                ).permitAll()      // ✅ allow Prometheus & health checks
//                
////              .permitAll())  // allows anyone to access them without authentication
//            	.anyRequest().authenticated())  // 🔒 everything else requires login
//          .csrf(AbstractHttpConfigurer::disable)
//          .formLogin(form -> form.permitAll())           // optional: enables default login page
//          .httpBasic(Customizer.withDefaults());          // optional: enables HTTP Basic Auth;
//        return http.build();
//    }
    
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

	    http
	        .authorizeHttpRequests(auth -> auth
	            .requestMatchers(
	                "/actuator/prometheus",
	                "/actuator/health",
	                "/actuator/info"
	            ).permitAll()
	            .anyRequest().authenticated()
	        )
	        .csrf(csrf -> csrf.disable())
	        .httpBasic(Customizer.withDefaults());  // ← add this;
//	        .oauth2ResourceServer(oauth -> oauth
//	            .jwt(jwt -> jwt
//	                .jwkSetUri("http://auth-server:7001/oauth2/jwks")
//	            )
//	        );

	    return http.build();
	}
}
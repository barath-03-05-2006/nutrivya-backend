package com.nutritrack.config;

import com.nutritrack.security.JwtFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.core.env.Environment;
import org.springframework.web.cors.*;
import java.util.*;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired private Environment env;

    @Autowired private JwtFilter jwtFilter;

    @Bean public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean public AuthenticationManager authManager(AuthenticationConfiguration c) throws Exception {
        return c.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(c -> c.configurationSource(corsSource()))
            .csrf(c -> c.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(a -> a
                // Only login is fully public
            		.requestMatchers(
            		        "/api/auth/login",
            		        "/api/auth/register",
            		        "/api/auth/forgot-password",
            		        "/api/auth/reset-password"
            		    ).permitAll()
                // OPTIONS preflight always allowed
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // Everything else needs JWT
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsSource() {
        CorsConfiguration c = new CorsConfiguration();
        // Read allowed origins from application.properties (app.frontend.url)
        // Falls back to localhost:3000 for local development.
        // In production, set app.frontend.url=https://nutrivya.co.in in your Render env vars.
        String frontendUrl = env.getProperty("app.frontend.url", "http://localhost:3000");
        c.setAllowedOriginPatterns(List.of(
            frontendUrl,
            "http://localhost:3000",   // local React dev
            "http://localhost:3001",   // alternate local port
            "http://localhost:[*]",    // local Flutter web testing (any port)
            "https://nutrivya.co.in", // production domain
            "https://*.vercel.app"    // Vercel preview deployments
        ));
        c.setAllowedMethods(Arrays.asList("GET","POST","PUT","DELETE","OPTIONS","PATCH"));
        c.setAllowedHeaders(List.of("*"));
        c.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource s = new UrlBasedCorsConfigurationSource();
        s.registerCorsConfiguration("/**", c);
        return s;
    }
}

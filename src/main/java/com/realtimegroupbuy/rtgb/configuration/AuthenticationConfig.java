package com.realtimegroupbuy.rtgb.configuration;

import com.realtimegroupbuy.rtgb.configuration.filter.JwtTokenFilter;
import com.realtimegroupbuy.rtgb.service.user.UserService;
import com.realtimegroupbuy.rtgb.util.JwtTokenUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class AuthenticationConfig {

    private final JwtTokenUtils jwtTokenUtils;
    private final UserService userService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/*/users/join", "/api/*/users/login").permitAll()
                .requestMatchers("/api/*/sellers/join", "/api/*/sellers/login").permitAll()
                .anyRequest().authenticated())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(new JwtTokenFilter(jwtTokenUtils, userService), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
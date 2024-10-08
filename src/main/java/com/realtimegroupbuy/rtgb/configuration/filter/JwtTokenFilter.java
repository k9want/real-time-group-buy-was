package com.realtimegroupbuy.rtgb.configuration.filter;

import com.realtimegroupbuy.rtgb.model.Seller;
import com.realtimegroupbuy.rtgb.model.User;
import com.realtimegroupbuy.rtgb.model.enums.UserRole;
import com.realtimegroupbuy.rtgb.service.seller.SellerService;
import com.realtimegroupbuy.rtgb.service.user.UserService;
import com.realtimegroupbuy.rtgb.util.JwtTokenUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    private final JwtTokenUtils jwtTokenUtils;
    private final UserService userService;
    private final SellerService sellerService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {

        final String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String token = header.split(" ")[1].trim();
            if (jwtTokenUtils.isTokenExpired(token)) {
                filterChain.doFilter(request, response);
                return;
            }

            String userName = jwtTokenUtils.getUserName(token);
            String role = jwtTokenUtils.getRole(token);
            if (role.equals(UserRole.USER.toString())) {
                User user = userService.loadUserByUsername(userName);
                GrantedAuthority userAuthority = new SimpleGrantedAuthority("ROLE_USER");
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    user, null, Collections.singletonList(userAuthority));
                authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else if (role.equals(UserRole.SELLER.toString())) {
                Seller seller = sellerService.loadSellerByUsername(userName);
                GrantedAuthority sellerAuthority = new SimpleGrantedAuthority("ROLE_SELLER");
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    seller, null, Collections.singletonList(sellerAuthority));
                authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }


        } catch (RuntimeException e) {
            log.error("Authentication error: {}", e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
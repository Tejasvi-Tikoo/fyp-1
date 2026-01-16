package com.fyp.project.config;

import java.io.IOException;

import javax.crypto.SecretKey;

import java.util.List;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtTokenValidator extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)throws ServletException, IOException {

        String jwt = request.getHeader(JwtConstant.JWT_HEADER);
        //it comes as bearer jwttoken

        if(jwt != null && jwt.startsWith("Bearer ")) {
            jwt = jwt.substring(7);
            try {
                //validate the token
                SecretKey key= Keys.hmacShaKeyFor(JwtConstant.SECRET_KEY.getBytes());
                Claims claims = Jwts.parser()
                                    .verifyWith(key)
                                    .build()
                                    .parseSignedClaims(jwt)
                                    .getPayload();

                String email= String.valueOf(claims.get("email"));
                String authorities = String.valueOf(claims.get("authorities"));

                List<GrantedAuthority> auths = AuthorityUtils.commaSeparatedStringToAuthorityList(authorities);
                Authentication authentication = new UsernamePasswordAuthenticationToken(email, auths);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                //if invalid, send error response
                //response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT Token");
                throw new BadCredentialsException("invalid token");
            }
            filterChain.doFilter(request, response);
        } else {
            //if no token present, send error response
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or Invalid Authorization header");
            return;
        }   

    }
    
}

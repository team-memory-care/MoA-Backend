package com.example.moabackend.global.security.filter;

import com.example.moabackend.domain.user.code.UserErrorCode;
import com.example.moabackend.global.code.GlobalErrorCode;
import com.example.moabackend.global.constant.Constants;
import com.example.moabackend.global.exception.CustomException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
public class JwtExceptionFilter extends OncePerRequestFilter {
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String requestURI = request.getRequestURI();
        return Constants.NO_NEED_AUTH.stream()
                .anyMatch(pattern -> antPathMatcher.match(pattern, requestURI));
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (SecurityException e) {
            log.error("FilterException throw SecurityException Exception : {}", e.getMessage());
            request.setAttribute("exception", UserErrorCode.INVALID_USER);
            filterChain.doFilter(request, response);
        } catch (MalformedJwtException e) {
            log.error("FilterException throw MalformedJwtException Exception : {}", e.getMessage());
            request.setAttribute("exception", GlobalErrorCode.TOKEN_MALFORMED_ERROR);
            filterChain.doFilter(request, response);
        } catch (IllegalArgumentException e) {
            log.error("FilterException throw IllegalArgumentException Exception : {}", e.getMessage());
            request.setAttribute("exception", GlobalErrorCode.TOKEN_TYPE_ERROR);
            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            log.error("FilterException throw ExpiredJwtException Exception : {}", e.getMessage());
            request.setAttribute("exception", GlobalErrorCode.EXPIRED_TOKEN_ERROR);
            filterChain.doFilter(request, response);
        } catch (UnsupportedJwtException e) {
            log.error("FilterException throw UnsupportedJwtException Exception : {}", e.getMessage());
            request.setAttribute("exception", GlobalErrorCode.TOKEN_UNSUPPORTED_ERROR);
            filterChain.doFilter(request, response);
        } catch (JwtException e) {
            log.error("FilterException throw JwtException Exception : {}", e.getMessage());
            request.setAttribute("exception", GlobalErrorCode.TOKEN_UNKNOWN_ERROR);
            filterChain.doFilter(request, response);
        } catch (CustomException e) {
            log.error("FilterException throw BaseException Exception : {}", e.getMessage());
            request.setAttribute("exception", e.getErrorCode());
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("FilterException throw Exception Exception : {}", e.getMessage());
            request.setAttribute("exception", GlobalErrorCode.INTERNAL_SERVER_ERROR);
            filterChain.doFilter(request, response);
        }
    }
}

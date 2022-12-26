package cric.champs.security.filter;

import cric.champs.customexceptions.InsufficientTimeException;
import cric.champs.security.userdetails.JWTUserDetailsService;
import cric.champs.security.utility.JWTUtility;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JWTUtility jwtUtility;

    @Autowired
    private JWTUserDetailsService jwtUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException, JwtException {
        try {
            String authorization = httpServletRequest.getHeader("Authorization");

            String token = null;
            String email = null;

            if (null != authorization && authorization.startsWith("Bearer ")) {
                token = authorization.substring(7);
                email = jwtUtility.getUsernameFromToken(token);
            }

                jwtUserDetailsService.checkTokenExistInBlocklist(token);

            if (null != email && SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(email);
                if (jwtUtility.validateToken(token, userDetails)) {
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken
                            = new UsernamePasswordAuthenticationToken(userDetails,
                            null, userDetails.getAuthorities());
                    usernamePasswordAuthenticationToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(httpServletRequest)
                    );
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                }
            }
        } catch (ExpiredJwtException exception) {
            String isRefreshToken = httpServletRequest.getHeader("isRefreshToken");
            String url = httpServletRequest.getRequestURL().toString();

            if (isRefreshToken != null && isRefreshToken.equalsIgnoreCase("true") && url.contains("refresh-token"))
                allowForGenerateRefreshToken(exception, httpServletRequest);
            else
                httpServletRequest.setAttribute("exception", "JWT_TOKEN_EXPIRED");
        } catch (BadCredentialsException exception) {
            httpServletRequest.setAttribute("exception", "BAD_CREDENTIALS");
        } catch (MalformedJwtException | InsufficientTimeException exception) {
            httpServletRequest.setAttribute("exception", "INVALID_JWT_TOKEN");
        } catch (UnsupportedJwtException exception) {
            httpServletRequest.setAttribute("exception", "Signed JWTs are not supported");
        } catch (SignatureException exception) {
            httpServletRequest.setAttribute("exception", "UNSUPPORTED_SIGNATURE_ALGORITHM");
        } catch (IllegalArgumentException exception) {
            httpServletRequest.setAttribute("exception", "INVALID_USER_DETAILS");
        }
        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }

    private void allowForGenerateRefreshToken(ExpiredJwtException exception, HttpServletRequest httpServletRequest) {
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken
                = new UsernamePasswordAuthenticationToken(null,
                null, null);
        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
        httpServletRequest.setAttribute("claims", exception.getClaims());
    }
}

package com.prog.config.security;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prog.handler.GenericResponse;
import com.prog.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
	
	private final UserDetailsService userDetailsService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {

		try {

			log.info("JwtFilter : doFilterInternal() : Processing request {}", request.getRequestURI());

			String authHeader = request.getHeader("Authorization");

			String token = null;
			String username = null;

			if (authHeader != null && authHeader.startsWith("Bearer ")) {
				token = authHeader.substring(7);
				username = jwtService.extractUsername(token);

				log.info("JwtFilter : doFilterInternal() : JWT token received for user={}", username);
			}

			if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

				UserDetails userDetails = userDetailsService.loadUserByUsername(username);

				Boolean validateToken = jwtService.validateToken(token, userDetails);

				if (validateToken) {

					UsernamePasswordAuthenticationToken authentication =
							new UsernamePasswordAuthenticationToken(
									userDetails,
									null,
									userDetails.getAuthorities());

					authentication.setDetails(
							new WebAuthenticationDetailsSource().buildDetails(request));

					SecurityContextHolder.getContext().setAuthentication(authentication);

					log.info("JwtFilter : doFilterInternal() : User authenticated successfully. Username={}",
							username);
				}
			}

		} catch (Exception e) {

			log.warn("JwtFilter : doFilterInternal() : JWT authentication failed. Reason={}",
					e.getMessage());

			generateResponseError(response, e);
			return;
		}

		filterChain.doFilter(request, response);
	}

	private void generateResponseError(HttpServletResponse response, Exception e)
			throws IOException {

		response.setContentType("application/json");
		response.setStatus(HttpStatus.UNAUTHORIZED.value());

		Object error = GenericResponse.builder()
				.status("failed")
				.message(e.getMessage())
				.responseStatus(HttpStatus.UNAUTHORIZED)
				.build()
				.create()
				.getBody();

		response.getWriter().write(new ObjectMapper().writeValueAsString(error));
	}

}
package org.meltzg.edhd.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

@Configuration
public class SecurityConfig {

	@Configuration
	@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
	protected static class SecurityConfiguration extends WebSecurityConfigurerAdapter {
		
		@Autowired
		AbstractSecurityService securityService;
		
		@Override
		protected void configure(HttpSecurity http) throws Exception {
			http.httpBasic().authenticationEntryPoint(new EdHDAuthenticationEntryPoint()).and().formLogin()
					.successHandler(new EdHDAuthenticationSuccessHandler())
					.failureHandler(new EdHDAuthenticationFailureHandler()).and().logout().and().authorizeRequests()
					.antMatchers("/", "/login**", "/register**", "/bower_components/**", "/src/**").permitAll().anyRequest()
					.authenticated().and().csrf().disable();
		}
		
		@Override
		protected void configure(AuthenticationManagerBuilder auth) throws Exception {
			auth.userDetailsService(securityService);
		}
	}

	protected static class EdHDAuthenticationEntryPoint implements AuthenticationEntryPoint {

		@Override
		public void commence(HttpServletRequest request, HttpServletResponse response,
				AuthenticationException authException) throws IOException, ServletException {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
		}

	}

	protected static class EdHDAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
		@Override
		public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
				Authentication authentication) throws IOException, ServletException {
			// This is actually not an error, but an OK message. It is sent to avoid
			// redirects.
			response.sendError(HttpServletResponse.SC_OK);
		}
	}

	protected static class EdHDAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

		@Override
		public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
				AuthenticationException exception) throws IOException, ServletException {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication Failed: " + exception.getMessage());
		}
	}
}

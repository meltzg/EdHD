package org.meltzg.edhd;

import java.security.Principal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableOAuth2Sso
@RestController
public class EdhdApplication extends WebSecurityConfigurerAdapter {
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.antMatcher("/**")
			.authorizeRequests()
				.antMatchers("/", "/login**", "/bower_components/**", "/src/**")
				.permitAll()
			.anyRequest()
				.authenticated()
			.and().logout().logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler())
			.and().csrf()
				.ignoringAntMatchers("/logout")
				.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
	}

	public static void main(String[] args) {
		SpringApplication.run(EdhdApplication.class, args);
	}
}

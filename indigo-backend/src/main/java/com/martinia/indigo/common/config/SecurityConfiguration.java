package com.martinia.indigo.common.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.martinia.indigo.common.login.filters.JWTAuthenticationFilter;
import com.martinia.indigo.common.login.filters.JWTAuthorizationFilter;
import com.martinia.indigo.common.login.service.LoginService;
import com.martinia.indigo.common.login.utils.JWTParserComponent;
import com.martinia.indigo.common.error.infrastructure.Http401UnauthorizedEntryPoint;

@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

	@Autowired
	JWTParserComponent jwtParserComponent;

	@Autowired
	LoginService userService;

	@Autowired
	Http401UnauthorizedEntryPoint authenticationEntryPoint;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userService);
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf()
				.disable()
				.cors()
				.and()
				.authorizeRequests()
				.antMatchers(HttpMethod.POST, "/api/login")
				.permitAll()
				.antMatchers(HttpMethod.GET, "/**")
				.permitAll()
				.anyRequest()
				.authenticated()
				.and()
				.addFilterBefore(
						new JWTAuthenticationFilter("/api/login", authenticationManager(), jwtParserComponent),
						UsernamePasswordAuthenticationFilter.class)
				.addFilterBefore(new JWTAuthorizationFilter(authenticationManager(), jwtParserComponent),
						UsernamePasswordAuthenticationFilter.class)
				.exceptionHandling()
				.authenticationEntryPoint(authenticationEntryPoint)
				.and()
				.sessionManagement()
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS);

	}

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowCredentials(true);
		configuration.setAllowedOriginPatterns(Arrays.asList("*"));		
		configuration.addAllowedHeader("*");
		configuration.addExposedHeader("Authorization");
		configuration.addAllowedMethod("*");
		configuration.setMaxAge(36000L);

		
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

}

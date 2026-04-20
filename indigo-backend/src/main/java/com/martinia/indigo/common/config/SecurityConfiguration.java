package com.martinia.indigo.common.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
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
public class SecurityConfiguration {

	private final JWTParserComponent jwtParserComponent;
	private final LoginService userService;
	private final Http401UnauthorizedEntryPoint authenticationEntryPoint;

	public SecurityConfiguration(JWTParserComponent jwtParserComponent,
			LoginService userService,
			Http401UnauthorizedEntryPoint authenticationEntryPoint) {
		this.jwtParserComponent = jwtParserComponent;
		this.userService = userService;
		this.authenticationEntryPoint = authenticationEntryPoint;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
		return authConfig.getAuthenticationManager();
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationManager authManager) throws Exception {
		http
				.csrf(AbstractHttpConfigurer::disable)
				.cors(cors -> cors.configurationSource(corsConfigurationSource()))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(HttpMethod.POST, "/api/login").permitAll()
						.requestMatchers("/api/config/**").hasAuthority("ADMIN")
						.requestMatchers("/api/metadata/**").hasAuthority("ADMIN")
						.requestMatchers("/api/file/**").hasAuthority("ADMIN")
						.requestMatchers(HttpMethod.GET, "/api/mail/test").hasAuthority("ADMIN")
						.requestMatchers(HttpMethod.GET, "/api/user/getAll").hasAuthority("ADMIN")
						.requestMatchers(HttpMethod.POST, "/api/user/save").hasAuthority("ADMIN")
						.requestMatchers(HttpMethod.DELETE, "/api/user/delete").hasAuthority("ADMIN")
						.requestMatchers("/api/user/me").authenticated()
						.requestMatchers(HttpMethod.GET, "/api/user/get").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/user/getById").permitAll()
						.requestMatchers("/api/user/**").authenticated()
						.requestMatchers("/api/notification/**").authenticated()
						.requestMatchers("/api/book/sent").authenticated()
						.requestMatchers("/api/book/recommendations/user").authenticated()
						.requestMatchers("/api/book/recommendations/user/count").authenticated()
						.requestMatchers(HttpMethod.GET, "/**").permitAll()
						.anyRequest().authenticated()
				)
				.addFilterBefore(new JWTAuthenticationFilter("/api/login", authManager, jwtParserComponent),
						UsernamePasswordAuthenticationFilter.class)
				.addFilterBefore(new JWTAuthorizationFilter(authManager, jwtParserComponent),
						UsernamePasswordAuthenticationFilter.class)
				.exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint))
				.sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		return http.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowCredentials(true);
		configuration.setAllowedOriginPatterns(Arrays.asList("http://localhost:4200", "http://krahen.synology.me:8080"));
		configuration.addAllowedHeader("*");
		configuration.addExposedHeader("Authorization");
		configuration.addAllowedMethod("*");
		configuration.setMaxAge(36000L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}

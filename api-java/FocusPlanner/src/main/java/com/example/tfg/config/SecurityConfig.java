package com.example.tfg.config;

import com.example.tfg.GogleCalendar.Auth.Permisos.OAuth2LoginSuccessHandler;
import com.example.tfg.security.Jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.web.filter.ForwardedHeaderFilter;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	@Value("${spring.security.oauth2.client.registration.google.client-id}")
	private String googleClientId;

	@Value("${spring.security.oauth2.client.registration.google.client-secret}")
	private String googleClientSecret;

	@Value("${spring.security.oauth2.client.registration.google.scope}")
	private String googleScope;

//	@Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
//	private String googleRedirectUri;


	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http,@Lazy OAuth2LoginSuccessHandler oauth2LoginSuccessHandler) throws Exception {

		return http.csrf(csrf -> csrf.disable()) // Desactivar CSRF para permitir POST desde Swagger
				.authorizeHttpRequests(auth -> auth.requestMatchers("/api/auth/**").permitAll() // Permitir autenticación pública
						.requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/swagger-resources/**").permitAll().requestMatchers("/api/users/**").authenticated().requestMatchers("/", "/login", "/error", "/test/**","/dashboard").permitAll().requestMatchers("/oauth2/token").permitAll().requestMatchers("/api/users/profile").authenticated().requestMatchers("/oauth2/callback/**").permitAll().requestMatchers("/api/google/**").permitAll()// Añadir esto para que la ruta de callback sea pública
						.requestMatchers("/oauth2/**").permitAll()
						.requestMatchers("/login/**").permitAll()
						.anyRequest().authenticated() // Proteger todo lo demás
				)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.oauth2Login(oauth2 -> oauth2
						.clientRegistrationRepository(clientRegistrationRepository())
						.defaultSuccessUrl("/dashboard", true)
						.authorizedClientService(authorizedClientService(clientRegistrationRepository()))
						.loginPage("/oauth2/authorization/google")
						.successHandler(oauth2LoginSuccessHandler) // Configura el handler para login exitoso
						.failureUrl("/login?error=true")
						.redirectionEndpoint(redirection -> redirection.baseUri("/api/google/callback"))
						.failureHandler((request, response, exception) -> {
							String errorMessage = URLEncoder.encode(exception.getMessage(), StandardCharsets.UTF_8);
							response.sendRedirect("/login?error=" + errorMessage);
						})
				)
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
				.build();
	}

	@Bean
	public OAuth2AuthorizedClientService authorizedClientService(ClientRegistrationRepository clientRegistrationRepository) {
		return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
	}

	@Bean
	public ClientRegistrationRepository clientRegistrationRepository() {
		return new InMemoryClientRegistrationRepository(this.googleClientRegistration());
	}
	@Bean
	public FilterRegistrationBean<ForwardedHeaderFilter> forwardedHeaderFilter() {
		FilterRegistrationBean<ForwardedHeaderFilter> filterRegBean = new FilterRegistrationBean<>();
		filterRegBean.setFilter(new ForwardedHeaderFilter());
		return filterRegBean;
	}


	private ClientRegistration googleClientRegistration() {
		return ClientRegistration.withRegistrationId("google")
				.clientId(googleClientId)
				.clientSecret(googleClientSecret)
				.scope(
						"openid",
						"profile",
						"email",
						"https://www.googleapis.com/auth/calendar"
				)
				.jwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
				.authorizationUri("https://accounts.google.com/o/oauth2/v2/auth?access_type=offline&prompt=consent")
				.tokenUri("https://oauth2.googleapis.com/token")
				.userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
				.userNameAttributeName("sub")
				.clientName("Google")
				.redirectUri("https://8a29-92-189-98-92.ngrok-free.app/api/google/callback") // Cambia esto a la URL de tu callback
				.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
				.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
				.build();
	}


	@Bean
	public RestTemplate restTemplate() {
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getMessageConverters().add(new FormHttpMessageConverter());
		return restTemplate;
	}

	@Bean
	public RestTemplateCustomizer restTemplateCustomizer() {
		return restTemplate -> restTemplate.getMessageConverters().add(new FormHttpMessageConverter());
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(List.of("http://localhost:8080"));
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
		configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(); // Usar BCrypt para encriptar contraseñas
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
		return authConfig.getAuthenticationManager();
	}
}

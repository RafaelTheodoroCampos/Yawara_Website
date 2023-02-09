package br.com.yawarasolution.config;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.yawarasolution.service.UserDetailsServiceImpl;
import br.com.yawarasolution.utils.AuthenticationEntryPointJwt;
import br.com.yawarasolution.utils.AuthenticationTokenFilter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

  @Autowired
  UserDetailsServiceImpl userDetailsService;

  @Autowired
  private AuthenticationEntryPointJwt unauthorizedHandler;

  @Bean
  public AuthenticationTokenFilter authenticationJwtTokenFilter() {
    return new AuthenticationTokenFilter();
  }

  @Bean
  public DaoAuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

    authProvider.setUserDetailsService(userDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder());

    return authProvider;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
    return authConfig.getAuthenticationManager();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  protected SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

    http.cors().and().csrf().disable().authorizeHttpRequests()
        .requestMatchers(HttpMethod.POST, "/api/auth/signin").permitAll()
        .requestMatchers(HttpMethod.POST, "/api/auth/signup").permitAll()
        .requestMatchers(HttpMethod.POST, "/api/auth/refreshtoken").permitAll()
        .requestMatchers(HttpMethod.POST, "/api/password-recovery/**").permitAll()
        .requestMatchers(HttpMethod.PUT, "/api/users/update/email/confirm").permitAll()
        .requestMatchers(HttpMethod.PUT, "/api/auth/confirm-account").permitAll()
        .requestMatchers(HttpMethod.GET, "/api/category").permitAll()
        .requestMatchers(HttpMethod.GET, "/api/category/{id}").permitAll()
        .requestMatchers(HttpMethod.GET, "/api/products").permitAll()
        .requestMatchers(HttpMethod.GET, "/api/products/pageable").permitAll()
        .requestMatchers(HttpMethod.GET, "/api/products/{id}").permitAll()
        .requestMatchers(HttpMethod.GET, "/api/category/name/{name}").permitAll()
        .requestMatchers("/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()
        .anyRequest()
        .authenticated().and().exceptionHandling().accessDeniedHandler(new AccessDeniedHandlerImpl()).and()
        .sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS).and().exceptionHandling()
        .authenticationEntryPoint(unauthorizedHandler);

    http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }

  public class AccessDeniedHandlerImpl implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex)
        throws IOException, ServletException {
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);

      Map<String, Object> body = new HashMap<>();
      body.put("status", HttpServletResponse.SC_FORBIDDEN);
      body.put("error", "Forbidden");
      body.put("message", "Access Denied");
      body.put("path", request.getServletPath());

      final ObjectMapper mapper = new ObjectMapper();
      mapper.writeValue(response.getOutputStream(), body);
    }
  }

}
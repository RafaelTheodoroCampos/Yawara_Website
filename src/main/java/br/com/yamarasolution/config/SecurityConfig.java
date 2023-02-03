package br.com.yamarasolution.config;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class SecurityConfig {


  @Bean
  protected SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

    http.cors().and().csrf().disable().authorizeHttpRequests()
        .requestMatchers("/api/auth/signin", "/api/auth/signup", "/api/auth/refreshtoken", "/api/auth/confirm-account",
            "/api/password-recovery/**")
        .permitAll()
        .requestMatchers("/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()
        .anyRequest()
        .authenticated().and().exceptionHandling().accessDeniedHandler(new AccessDeniedHandlerImpl());

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

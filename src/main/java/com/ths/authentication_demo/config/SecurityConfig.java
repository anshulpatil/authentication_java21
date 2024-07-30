package com.ths.authentication_demo.config;

import com.ths.authentication_demo.jwt.JwtAuthFilter;
import com.ths.authentication_demo.service.UserDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
  private final UserDetailService userDetailsService;
  private final JwtAuthFilter jwtAuthFilter;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain filterChain(
    HttpSecurity http,
    AuthenticationManager authenticationManager
  )
    throws Exception {
    return http
      .cors(AbstractHttpConfigurer::disable)
      .csrf(AbstractHttpConfigurer::disable)
      .sessionManagement(
        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      )
      //        Set permissions on endpoints
      .authorizeHttpRequests(
        auth ->
          auth
            .requestMatchers(HttpMethod.POST, "/api/auth/signup").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/auth/forget-password").permitAll()
                  //endpoints for views
            .requestMatchers(HttpMethod.GET, "/view/login/**").permitAll()
            .requestMatchers(HttpMethod.POST, "/view/login/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/view/forget-password/**").permitAll()
            .requestMatchers(HttpMethod.POST, "/view/forget-password/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/view/index/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/view/register/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/view/register?success").permitAll()
            .requestMatchers(HttpMethod.POST, "/view/register/save/**").permitAll()
            .anyRequest()
            .authenticated()
      )
//      .formLogin(
//         form ->
//            form
//              .loginPage("/view/login")
//              .loginProcessingUrl("/view/login")
//              .defaultSuccessUrl("/view/users")
//              .permitAll()
//      )
      .authenticationManager(authenticationManager)
      .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
      .build();
  }

  @Bean
  public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
    AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(
      AuthenticationManagerBuilder.class
    );
    authenticationManagerBuilder
      .userDetailsService(userDetailsService)
      .passwordEncoder(passwordEncoder());
    return authenticationManagerBuilder.build();
  }
}

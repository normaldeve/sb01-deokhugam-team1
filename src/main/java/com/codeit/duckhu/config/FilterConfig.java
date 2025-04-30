package com.codeit.duckhu.config;

import com.codeit.duckhu.domain.user.UserAuthenticationFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {
  @Bean
  FilterRegistrationBean<UserAuthenticationFilter> userAuthFilter(UserAuthenticationFilter filter) {
    FilterRegistrationBean<UserAuthenticationFilter> registration = new FilterRegistrationBean<>();
    registration.setFilter(filter);
    registration.addUrlPatterns("/api/*");
    registration.setOrder(1);
    return registration;
  }
  ;
}

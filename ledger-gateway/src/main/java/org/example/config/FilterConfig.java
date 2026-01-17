package org.example.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {
    @Bean
    public FilterRegistrationBean<JwtFilter> jwtFilterRegistration(JwtFilter jwtFilter) {
        FilterRegistrationBean<JwtFilter> registrationBean = new FilterRegistrationBean<>();

        registrationBean.setFilter(jwtFilter);

        // Apply filter to secured paths
        registrationBean.addUrlPatterns("/api/payments/*");
        registrationBean.addUrlPatterns("/api/account/*");
        registrationBean.addUrlPatterns("/api/internal/*");
        registrationBean.addUrlPatterns("/api/payments/*");
        // Add other protected paths here

        return registrationBean;
    }
}

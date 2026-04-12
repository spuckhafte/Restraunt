package com.swe.backend.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.swe.backend.auth.AuthInterceptor;
import com.swe.backend.auth.AuthPolicyService;
import com.swe.backend.service.AuthService;

@Configuration
@ConditionalOnProperty(name = "app.auth.enabled", havingValue = "true", matchIfMissing = true)
public class AuthWebConfig implements WebMvcConfigurer {
    private final AuthInterceptor authInterceptor;

    private static AuthInterceptor createAuthInterceptor(AuthService authService, AuthPolicyService authPolicyService) {
        return new AuthInterceptor(authService, authPolicyService);
    }

    public AuthWebConfig(AuthService authService, AuthPolicyService authPolicyService) {
        this.authInterceptor = createAuthInterceptor(authService, authPolicyService);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
            .addPathPatterns("/api/**")
            .excludePathPatterns("/api/auth/login");
    }
}

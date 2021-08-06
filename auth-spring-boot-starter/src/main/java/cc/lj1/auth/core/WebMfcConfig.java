package cc.lj1.auth.core;

import cc.lj1.auth.properties.AuthProperties;
import cc.lj1.auth.services.AuthTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebMfcConfig implements WebMvcConfigurer {
    @Autowired
    private AuthTokenService authTokenService;

    @Autowired
    AuthProperties authProperties;

    @Autowired
    AuthArgumentResolver authArgumentResolver;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthInterceptor(authTokenService, authProperties))
                .addPathPatterns("/**");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(authArgumentResolver);
        WebMvcConfigurer.super.addArgumentResolvers(resolvers);
    }
}

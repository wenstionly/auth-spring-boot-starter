package cc.lj1.auth.support;

import cc.lj1.auth.interceptor.AuthInterceptor;
import cc.lj1.auth.properties.AuthProperties;
import cc.lj1.auth.utils.AuthUtils;
import cc.lj1.auth.utils.CacheUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableConfigurationProperties({
        AuthProperties.class
})
@ConditionalOnClass(WebMvcConfigurer.class)
public class AuthFactory {

    @Bean
    @ConditionalOnProperty(prefix = "cc.lj1.auth", name = "enable", havingValue = "true")
    public WebMvcConfigurer authInterceptor() {
        return new AuthInterceptor();
    }

    @Bean
    @ConditionalOnProperty(prefix = "cc.lj1.auth", name = "enable", havingValue = "true")
    public AuthUtils authUtils() {
        return new AuthUtils();
    }

    @Bean
    @ConditionalOnProperty(prefix = "cc.lj1.auth", name = "enable", havingValue = "true")
    public CacheUtils cacheUtils() {
        return new CacheUtils();
    }
}

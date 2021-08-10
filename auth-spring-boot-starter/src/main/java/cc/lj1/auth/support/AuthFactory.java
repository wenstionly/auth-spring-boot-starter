package cc.lj1.auth.support;

import cc.lj1.auth.core.AuthArgumentResolver;
import cc.lj1.auth.core.WebMfcConfig;
import cc.lj1.auth.properties.AuthProperties;
import cc.lj1.auth.services.AuthTokenService;
import cc.lj1.auth.core.utils.CacheUtils;
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
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMfcConfig();
    }

    @Bean
    public AuthTokenService authTokenService() {
        return new AuthTokenService();
    }

    @Bean
    public CacheUtils cacheUtils() {
        return new CacheUtils();
    }

    @Bean
    public AuthArgumentResolver argumentResolver() {
        return new AuthArgumentResolver();
    }
}

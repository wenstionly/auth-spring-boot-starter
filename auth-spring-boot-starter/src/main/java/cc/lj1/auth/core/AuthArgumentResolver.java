package cc.lj1.auth.core;

import cc.lj1.auth.AuthenticatableUser;
import cc.lj1.auth.annotation.RequestCurrentUser;
import cc.lj1.auth.annotation.RequestAgentType;
import cc.lj1.auth.properties.AuthProperties;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class AuthArgumentResolver implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        // 检查参数是否具有指定的注解
        boolean assignable = methodParameter.getParameterType().isAssignableFrom(AuthenticatableUser.class);
        boolean isInstance = AuthenticatableUser.class.isAssignableFrom(methodParameter.getParameterType());
        boolean hasAnnotation = methodParameter.hasParameterAnnotation(RequestCurrentUser.class);

        boolean isAgentType = methodParameter.hasParameterAnnotation(RequestAgentType.class);

        return ((assignable || isInstance) && hasAnnotation) || isAgentType;
    }

    @Override
    public Object resolveArgument(
            MethodParameter methodParameter,
            ModelAndViewContainer modelAndViewContainer,
            NativeWebRequest nativeWebRequest,
            WebDataBinderFactory webDataBinderFactory
    ) {
        if(methodParameter.hasParameterAnnotation(RequestAgentType.class))
            return nativeWebRequest.getAttribute(AuthProperties.AGENT_KEY, RequestAttributes.SCOPE_REQUEST);
        else
            return nativeWebRequest.getAttribute(AuthProperties.USER_KEY, RequestAttributes.SCOPE_REQUEST);
    }
}

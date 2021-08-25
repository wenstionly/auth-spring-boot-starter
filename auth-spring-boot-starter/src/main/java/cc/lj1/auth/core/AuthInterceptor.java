package cc.lj1.auth.core;

import cc.lj1.auth.AuthenticatableRole;
import cc.lj1.auth.AuthenticatableUser;
import cc.lj1.auth.annotation.RequestAuthentication;
import cc.lj1.auth.annotation.RequestPermission;
import cc.lj1.auth.core.utils.PermissionUtils;
import cc.lj1.auth.exception.AuthFailedException;
import cc.lj1.auth.exception.AuthForbiddenException;
import cc.lj1.auth.properties.AuthProperties;
import cc.lj1.auth.services.AuthTokenService;
import eu.bitwalker.useragentutils.UserAgent;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

class AuthInterceptor implements HandlerInterceptor {
    private AuthHelper authHelper;

    public AuthInterceptor(AuthTokenService authTokenService, AuthProperties authProperties) {
        this.authHelper = new AuthHelper(authTokenService, authProperties);
    }
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 处理Agent信息
        authHelper.proceedAgentInfo(request);
        authHelper.proceedToken(request);
        if(handler instanceof HandlerMethod) {
            Class clz = ((HandlerMethod) handler).getBeanType();
            Method method = ((HandlerMethod) handler).getMethod();

            RequestAuthentication authForClz = (RequestAuthentication) clz.getAnnotation(RequestAuthentication.class);
            RequestAuthentication authForMethod = method.getAnnotation(RequestAuthentication.class);
            RequestPermission acForClz = (RequestPermission) clz.getAnnotation(RequestPermission.class);
            RequestPermission acForMethod = method.getAnnotation(RequestPermission.class);

            String acName = acForMethod != null ? acForMethod.value() : (acForClz != null ? acForClz.value() : null);
            boolean needAuth = (acName != null) || (authForMethod != null) || (authForClz != null);
            if(needAuth) {
                AuthenticatableUser user = authHelper.check(request);
                if(user == null)
                    throw new AuthFailedException();
                if(!PermissionUtils.check(user, acName))
                    throw new AuthForbiddenException();
            }
        }
        return true;
    }

    static private class AuthHelper {
        AuthTokenService authTokenService;
        AuthProperties authProperties;
        public AuthHelper(AuthTokenService authTokenService, AuthProperties authProperties) {
            this.authTokenService = authTokenService;
            this.authProperties = authProperties;
        }

        private String getTokenFromRequest(HttpServletRequest request) {
            String token = null;
            String headerKey = authProperties.getHeaderKey();
            String inputKey = authProperties.getInputKey();

            // 优先从header中获取
            if(StringUtils.hasLength(headerKey)) {
                token = request.getHeader(headerKey);
            }
            if(StringUtils.hasLength(inputKey)) {
                // 其次是query
                if(!StringUtils.hasLength(token)) {
                    token = request.getParameter(inputKey);
                }
                // TODO: 最后是body
            }
            return token;
        }

        public void proceedAgentInfo(HttpServletRequest request) {
            UserAgent userAgent = UserAgent.parseUserAgentString(request.getHeader("User-Agent"));
            switch (userAgent.getOperatingSystem().getDeviceType()) {
                case MOBILE:
                    request.setAttribute(AuthProperties.AGENT_KEY, "mobile");
                    break;
                case TABLET:
                    request.setAttribute(AuthProperties.AGENT_KEY, "tablet");
                    break;
                default:
                    request.setAttribute(AuthProperties.AGENT_KEY, "desktop");
                    break;
            }
        }

        public String proceedToken(HttpServletRequest request) {
            String token = getTokenFromRequest(request);
            request.setAttribute(AuthProperties.TOKEN_KEY, token);
            return token;
        }

        public AuthenticatableUser check(HttpServletRequest request) {
            AuthenticatableUser user = null;
            String token = (String)request.getAttribute(AuthProperties.TOKEN_KEY);
            if(token != null) {
                user = authTokenService.check(token, (String) request.getAttribute(AuthProperties.AGENT_KEY));
            }
            request.setAttribute(AuthProperties.USER_KEY, user);
            return user;
        }
    }
}

package cc.lj1.auth.core;

import cc.lj1.auth.AuthenticatableRole;
import cc.lj1.auth.AuthenticatableUser;
import cc.lj1.auth.annotation.RequestAuthentication;
import cc.lj1.auth.annotation.RequestPermission;
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
                if(!authHelper.checkPermission(user, acName))
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

        public AuthenticatableUser check(HttpServletRequest request) {
            AuthenticatableUser user = null;
            String token = getTokenFromRequest(request);
            if(token != null) {
                user = authTokenService.check(token, (String) request.getAttribute(AuthProperties.AGENT_KEY));
            }
            request.setAttribute(AuthProperties.USER_KEY, user);
            return user;
        }

        public boolean checkPermission(AuthenticatableUser user, String acName) {
            // 没有登录
            if(user == null) {
                return false;
            }
            // 超级用户或者不存在接口名称则不需要验证权限
            if(user.isSuper() || acName == null || acName.isEmpty()) {
                return true;
            }
            final AuthenticatableRole[] roles = user.getRoles();
            // 角色列表为空则一定失败
            if(roles == null && roles.length <= 0) {
                return false;
            }
            // 角色
            String[] acParts = acName.split("\\.");
            // 检查权限
            for(AuthenticatableRole role: roles) {
                if(checkRole(role, acParts)) {
                    return true;
                }
            }
            return false;
        }

        public boolean checkRole(AuthenticatableRole role, String[] acParts) {
            if(role == null) {
                return false;
            }
            if(role.isSuper()) {
                return true;
            }
            String[] permissions = role.getPermissions();
            if(permissions == null || permissions.length <= 0) {
                return false;
            }
            for(String permission: permissions) {
                if(checkPermission(permission, acParts))
                    return true;
            }
            return false;
        }

        public boolean checkPermission(String permission, String[] acParts) {
            // 拆分权限列表中的权限名称
            String[] permParts = permission.split("\\.");
            int pos = 0;
            // 逐级检查
            while(pos < acParts.length && pos < permParts.length) {
                if(permParts[pos].equals("*")) {
                    return true;
                }
                if(!permParts[pos].equals(acParts[pos]))
                    break;
                pos++;
            }
            return (pos == acParts.length) && (pos == permParts.length);
        }
    }
}

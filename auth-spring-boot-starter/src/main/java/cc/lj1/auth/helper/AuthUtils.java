package cc.lj1.auth.helper;

import cc.lj1.auth.AuthenticatableUser;
import cc.lj1.auth.core.utils.PermissionUtils;
import cc.lj1.auth.properties.AuthProperties;
import org.springframework.web.context.request.RequestAttributes;

public class AuthUtils {

    public static String token() {
        try {
            return (String) ServletUtils.getRequestAttributes().getAttribute(AuthProperties.TOKEN_KEY, RequestAttributes.SCOPE_REQUEST);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String agentType() {
        try {
            return (String) ServletUtils.getRequestAttributes().getAttribute(AuthProperties.AGENT_KEY, RequestAttributes.SCOPE_REQUEST);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static AuthenticatableUser currentUser() {
        try {
            return (AuthenticatableUser) ServletUtils.getRequestAttributes().getAttribute(AuthProperties.USER_KEY, RequestAttributes.SCOPE_REQUEST);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean hasPermission(String acName) {
        return PermissionUtils.check(currentUser(), acName);
    }

}

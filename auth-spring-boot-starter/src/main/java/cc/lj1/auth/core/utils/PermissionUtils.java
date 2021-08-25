package cc.lj1.auth.core.utils;

import cc.lj1.auth.AuthenticatableRole;
import cc.lj1.auth.AuthenticatableUser;

public class PermissionUtils {

    public static boolean check(AuthenticatableUser user, String acName) {
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

    private static boolean checkRole(AuthenticatableRole role, String[] acParts) {
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

    private static boolean checkPermission(String permission, String[] acParts) {
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

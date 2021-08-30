package cc.lj1.auth;

public interface AuthenticatableUser {
    default String getPrimaryKey() {
        return null;
    }

    default boolean isSuper() {
        return false;
    }

    // 验证权限时，优先使用此接口，如果返回null，则再调用getRoles
    default String[] getPermissions() {
        return null;
    }

    default AuthenticatableRole[] getRoles() {
        return null;
    }
}

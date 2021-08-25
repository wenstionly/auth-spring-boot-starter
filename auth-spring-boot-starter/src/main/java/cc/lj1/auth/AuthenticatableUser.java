package cc.lj1.auth;

public interface AuthenticatableUser {
    default String getPrimaryKey() {
        return null;
    }

    default boolean isSuper() {
        return false;
    }

    default AuthenticatableRole[] getRoles() {
        return null;
    }
}

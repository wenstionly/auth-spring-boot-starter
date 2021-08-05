package cc.lj1.auth;

public interface AuthenticatableUser {
    default String getId() {
        return null;
    }

    default boolean isSuper() {
        return false;
    }

    default AuthenticatableRole[] getRoles() {
        return null;
    }
}
